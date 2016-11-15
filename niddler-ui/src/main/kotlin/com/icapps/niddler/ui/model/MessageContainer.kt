package com.icapps.niddler.ui.model

import com.google.gson.Gson
import com.icapps.niddler.ui.NiddlerClientMessageListener
import java.util.*

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class MessageContainer(private var bodyParser: NiddlerMessageBodyParser) : NiddlerClientMessageListener {

    private val messages: MutableSet<ParsedNiddlerMessage> = hashSetOf()
    private val gson = Gson()
    private val listeners: MutableSet<NiddlerMessageListener> = hashSetOf()

    fun clear() {
        messages.clear()
    }

    fun addMessage(msg: ParsedNiddlerMessage) {
        synchronized(messages) {
            messages.add(msg)
        }
    }

    fun getMessagesChronological(): List<ParsedNiddlerMessage> {
        val sortedMessages = synchronized(messages) { ArrayList(messages) }
        sortedMessages.sortBy { it.timestamp }
        return sortedMessages
    }

    fun getMessagesLinked(): SortedMap<String, List<ParsedNiddlerMessage>> {
        val chronological = getMessagesChronological()
        val map: MutableMap<String, MutableList<ParsedNiddlerMessage>> = LinkedHashMap()
        chronological.forEach {
            var items = map[it.requestId]
            if (items == null) {
                items = arrayListOf(it)
                map[it.requestId] = items
            } else {
                items.add(it)
            }
        }
        @Suppress("UNCHECKED_CAST")
        return map as SortedMap<String, List<ParsedNiddlerMessage>>
    }

    override fun onMessage(msg: String) {
        val message = bodyParser.parseBody(gson.fromJson(msg, NiddlerMessage::class.java))
        addMessage(message)

        synchronized(listeners) {
            listeners.forEach { it.onMessage(message) }
        }
    }

    fun registerListener(listener: NiddlerMessageListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    fun unregisterListener(listener: NiddlerMessageListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

}

interface NiddlerMessageListener {

    fun onMessage(message: ParsedNiddlerMessage)

}