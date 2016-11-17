package com.icapps.niddler.ui.model

import com.google.gson.Gson
import com.icapps.niddler.ui.NiddlerClientMessageListener
import java.util.*

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class MessageContainer(private var bodyParser: NiddlerMessageBodyParser) : NiddlerClientMessageListener {

    private val knownMessageIds: MutableSet<String> = hashSetOf()
    private val messagesByMessageRequestId: MutableMap<String, MutableList<ParsedNiddlerMessage>> = hashMapOf()

    private val gson = Gson()
    private val listeners: MutableSet<NiddlerMessageListener> = hashSetOf()

    fun clear() {
        synchronized(knownMessageIds) {
            knownMessageIds.clear()
            messagesByMessageRequestId.clear()
        }
    }

    fun addMessage(msg: ParsedNiddlerMessage) {
        synchronized(knownMessageIds) {
            if (knownMessageIds.add(msg.messageId)) {
                var list = messagesByMessageRequestId[msg.requestId]
                if (list == null) {
                    list = arrayListOf(msg)
                    messagesByMessageRequestId[msg.requestId] = list
                } else {
                    list.add(msg)
                    list.sortBy { it.timestamp }
                }
            }
        }
    }

    fun getMessagesChronological(): List<ParsedNiddlerMessage> {
        val sortedMessages = ArrayList<ParsedNiddlerMessage>(knownMessageIds.size)
        synchronized(knownMessageIds) {
            messagesByMessageRequestId.forEach { it -> sortedMessages.addAll(it.value) }
        }
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
        if (!message.isControlMessage) {
            addMessage(message)
        }

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

    fun getMessagesWithRequestId(requestId: String): List<ParsedNiddlerMessage>? {
        return synchronized(knownMessageIds) { messagesByMessageRequestId[requestId] }
    }

}

interface NiddlerMessageListener {

    fun onMessage(message: ParsedNiddlerMessage)

}