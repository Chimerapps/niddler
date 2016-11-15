package com.icapps.niddler.ui.model

import org.java_websocket.util.Base64
import java.nio.charset.Charset


/**
 * @author Nicola Verbeeck
 * @date 14/11/16.
 */

class NiddlerMessage {

    var requestId: String? = null
    var url: String? = null
    var method: String? = null
    var body: String? = null
    var headers: Map<String, List<String>>? = null
    var statusCode: Int? = null

    val isRequest: Boolean
        get() = statusCode == null

    val isResponse: Boolean
        get() = !isRequest

    val getBodyAsString: String
        get() = String(Base64.decode(body!!), Charset.forName("UTF-8"))

}
