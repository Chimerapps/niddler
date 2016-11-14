package com.icapps.niddler.ui

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by maartenvangiel on 14/11/2016.
 */
class NiddlerClient(serverURI: URI?) : WebSocketClient(serverURI) {

    val logger: Logger = Logger.getLogger("NiddlerClient")

    override fun onOpen(handshakedata: ServerHandshake?) {
        logger.log(Level.FINE, "Connection opened: " + handshakedata)
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        logger.log(Level.FINE, "Connection closed: " + reason)
    }

    override fun onMessage(message: String?) {
        logger.log(Level.FINEST, "Got message: " + message)
    }

    override fun onError(ex: Exception?) {
        logger.log(Level.SEVERE, ex.toString())
    }

}