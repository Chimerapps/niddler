package com.icapps.niddler.ui

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

/**
 * Created by maartenvangiel on 14/11/2016.
 */
class NiddlerClient(serverURI: URI?) : WebSocketClient(serverURI) {

    override fun onOpen(handshakedata: ServerHandshake?) {
        print("Connection opened: " + handshakedata)
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        print("Connection closed: " + reason)
    }

    override fun onMessage(message: String?) {
        print("Got message: " + message)
    }

    override fun onError(ex: Exception?) {
        print(ex.toString())
    }

}