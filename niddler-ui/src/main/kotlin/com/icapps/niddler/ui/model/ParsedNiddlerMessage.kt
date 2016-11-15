package com.icapps.niddler.ui.model

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class ParsedNiddlerMessage(val bodyFormat: BodyFormat, val bodyData: Any?, val message: NiddlerMessage) {

    val subItems: List<ParsedNiddlerMessageSubItem>

    init {
        subItems = mutableListOf<ParsedNiddlerMessageSubItem>()
        if(headers != null){
            subItems.add(ParsedNiddlerMessageSubItem("headers", headers.toString()))
        }
        if(bodyData != null){
            subItems.add(ParsedNiddlerMessageSubItem("body", bodyData.toString()))
        }
    }

    val requestId: String
        get() = message.requestId

    val messageId: String
        get() = message.messageId

    val timestamp: Long
        get() = message.timestamp

    val url: String?
        get() = message.url

    val method: String?
        get() = message.method

    val body: String?
        get() = message.body

    val headers: Map<String, List<String>>?
        get() = message.headers

    val statusCode: Int?
        get() = message.statusCode

    val isRequest: Boolean
        get() = message.isRequest
}