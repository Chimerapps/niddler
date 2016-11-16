package com.icapps.niddler.ui.model

import org.java_websocket.util.Base64
import java.nio.charset.Charset


/**
 * @author Nicola Verbeeck
 * @date 14/11/16.
 */

open class NiddlerMessage {

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

    val getBodyAsBytes: ByteArray?
        get() = if (body != null) Base64.decode(body) else null

    fun getBodyAsString(encoding: String?): String? {
        return if (body != null)
            String(Base64.decode(body), if (encoding == null) Charsets.UTF_8 else Charset.forName(encoding))
        else
            null
    }

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
