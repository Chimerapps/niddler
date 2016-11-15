package com.icapps.niddler.ui.model

import com.google.gson.Gson
import com.icapps.niddler.ui.NiddlerClientMessageListener
import java.util.*

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class MessageContainer : NiddlerClientMessageListener {

    private val messages: MutableSet<NiddlerMessage> = hashSetOf()
    private val gson = Gson()
    private val listeners: MutableSet<NiddlerMessageListener> = hashSetOf()

    fun clear() {
        messages.clear()
    }

    fun addMessage(msg: NiddlerMessage) {
        synchronized(messages) {
            messages.add(msg)
        }
    }

    fun getMessagesChronological(): List<NiddlerMessage> {
        val sortedMessages = synchronized(messages) { ArrayList(messages) }
        sortedMessages.sortBy { it.timestamp }
        return sortedMessages
    }

    fun getMessagesLinked(): SortedMap<String, List<NiddlerMessage>> {
        val chronological = getMessagesChronological()
        val map: MutableMap<String, MutableList<NiddlerMessage>> = LinkedHashMap()
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
        return map as SortedMap<String, List<NiddlerMessage>>
    }

    override fun onMessage(msg: String) {
        val message = gson.fromJson(msg, NiddlerMessage::class.java)
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

    fun onMessage(message: NiddlerMessage)

}