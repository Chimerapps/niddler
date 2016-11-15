package com.icapps.niddler.ui.model

import org.java_websocket.util.Base64


/**
 * @author Nicola Verbeeck
 * @date 14/11/16.
 */

class NiddlerMessage {

    lateinit var requestId: String
    lateinit var messageId: String
    var timestamp: Long = 0

    var url: String? = null
    var method: String? = null
    var body: String? = null
    var headers: Map<String, List<String>>? = null
    var statusCode: Int? = null

    val isRequest: Boolean
        get() = statusCode == null

    val getBodyAsString: String?
        get() = if (body != null) String(Base64.decode(body), Charsets.UTF_8) else null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as NiddlerMessage

        if (messageId != other.messageId) return false

        return true
    }

    override fun hashCode(): Int {
        return messageId.hashCode()
    }


}
