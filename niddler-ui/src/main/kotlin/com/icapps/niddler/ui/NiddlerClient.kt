package com.icapps.niddler.ui

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import trikita.log.Log
import java.net.URI
import java.util.*

/**
 * Created by maartenvangiel on 14/11/2016.
 */
class NiddlerClient(serverURI: URI?) : WebSocketClient(serverURI) {

    private val clientListeners: MutableSet<NiddlerClientListener> = HashSet()
    private val messageListeners: MutableSet<NiddlerClientMessageListener> = HashSet()

    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.d("Connection succeeded: " + connection.remoteSocketAddress)
        synchronized(clientListeners) {
            clientListeners.forEach { it.onConnected() }
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d("Connection closed: " + reason)
    }

    override fun onMessage(message: String) {
        Log.d("Got message: " + message)
        synchronized(clientListeners) {
            messageListeners.forEach { it.onMessage(message) }
        }
    }

    override fun onError(ex: Exception?) {
        Log.d(ex.toString())
    }

    fun registerClientListener(listener: NiddlerClientListener) {
        synchronized(clientListeners) {
            clientListeners.add(listener)
        }
    }

    fun unregisterClientListener(listener: NiddlerClientListener) {
        synchronized(clientListeners) {
            clientListeners.remove(listener)
        }
    }

    fun registerMessageListener(listener: NiddlerClientMessageListener) {
        synchronized(messageListeners) {
            messageListeners.add(listener)
        }
    }

    fun unregisterMessageListener(listener: NiddlerClientMessageListener) {
        synchronized(messageListeners) {
            messageListeners.remove(listener)
        }
    }
}

interface NiddlerClientListener {
    fun onConnected()
}

interface NiddlerClientMessageListener {
    fun onMessage(msg: String)
}