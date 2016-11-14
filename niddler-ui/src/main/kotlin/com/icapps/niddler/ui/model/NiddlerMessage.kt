package com.icapps.niddler.ui.model

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

}
