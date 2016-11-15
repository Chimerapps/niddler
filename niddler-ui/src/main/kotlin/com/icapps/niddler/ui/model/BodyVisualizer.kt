package com.icapps.niddler.ui.model

import com.google.gson.JsonParser
import org.apache.http.entity.ContentType
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import javax.swing.JComponent
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class BodyVisualizer {

    companion object {
        private val FIRST_BYTE_SEARCH_RANGE_MAX: Int = 10
    }

    fun createBodyVisualizer(message: NiddlerMessage): JComponent {
        val format = classifyFormat(message)
        return createBodyVisualizer(message, format)
    }

    fun createBodyVisualizer(message: NiddlerMessage, format: BodyFormat): JComponent {
        TODO("Not yet implemented")
    }

    private fun classifyFormat(message: NiddlerMessage): BodyFormat {
        val contentTypeHeader = message.headers?.get("Content-Type")
        if (contentTypeHeader != null && !contentTypeHeader.isEmpty()) {
            val contentTypeString = contentTypeHeader[0]
            val parsedContentType = ContentType.parse(contentTypeString)
            return BodyFormat(fromMime(parsedContentType.mimeType), parsedContentType.mimeType, parsedContentType.charset?.name())
        }
        return determineTypeFromBody(message.getBodyAsBytes)
    }

    private fun determineTypeFromBody(bodyAsBytes: ByteArray?): BodyFormat {
        if (bodyAsBytes == null || bodyAsBytes.isEmpty())
            return BodyFormat.NONE

        val firstReasonableTextByte = findFirstReasonableTextByte(bodyAsBytes)
        when (firstReasonableTextByte) {
            '{'.toByte(), '['.toByte() -> if (tryJson(bodyAsBytes)) return BodyFormat(BodyFormatType.FORMAT_JSON, null, Charsets.UTF_8.name()) else return BodyFormat.UNKNOWN
            '<'.toByte() -> return examineXML(bodyAsBytes)
        }
        //TODO image
        return BodyFormat.UNKNOWN
    }

    private fun findFirstReasonableTextByte(bytes: ByteArray): Byte {
        val maxIndex = Math.min(bytes.size, FIRST_BYTE_SEARCH_RANGE_MAX)
        return (0..maxIndex - 1)
                .firstOrNull { bytes[it] > 32 }
                ?.let { bytes[it] }
                ?: bytes[0]
    }

    private fun fromMime(mimeType: String): BodyFormatType {
        when (mimeType.toLowerCase()) {
            "application/json" -> return BodyFormatType.FORMAT_JSON
            "application/xml", "text/xml" -> return BodyFormatType.FORMAT_XML
            "application/octet-stream" -> return BodyFormatType.FORMAT_BINARY
            "text/html", "text/plain" -> return BodyFormatType.FORMAT_PLAIN
            "application/svg+xml" -> return BodyFormatType.FORMAT_IMAGE
            "image/bmp", "image/png", "image/tiff" -> return BodyFormatType.FORMAT_IMAGE
        }
        return BodyFormatType.FORMAT_BINARY
    }

    private fun examineXML(bodyAsBytes: ByteArray): BodyFormat {
        try {
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(bodyAsBytes))

            when (document.documentElement.tagName) {
                "svg" -> return BodyFormat(BodyFormatType.FORMAT_IMAGE, "application/svg+xml", document.inputEncoding)
            }
            val asString = String(bodyAsBytes, 0, Math.min(FIRST_BYTE_SEARCH_RANGE_MAX, bodyAsBytes.size - 1), Charsets.UTF_8)
            if (asString.contains("<html"))
                return BodyFormat(BodyFormatType.FORMAT_XML, "application/html", null)
            return BodyFormat(BodyFormatType.FORMAT_XML, null, null)
        } catch(e: Exception) {
            return BodyFormat.UNKNOWN
        }
    }

    private fun tryJson(bodyAsBytes: ByteArray): Boolean {
        try {
            JsonParser().parse(InputStreamReader(ByteArrayInputStream(bodyAsBytes), Charsets.UTF_8))
            return true
        } catch(e: Exception) {
            return false
        }
    }
}

data class BodyFormat(val type: BodyFormatType, val subtype: String?, val encoding: String?) {
    companion object {
        val NONE = BodyFormat(BodyFormatType.FORMAT_BINARY, null, null)
        val UNKNOWN = BodyFormat(BodyFormatType.FORMAT_BINARY, null, null)
    }
}

enum class BodyFormatType {
    FORMAT_JSON,
    FORMAT_XML,
    FORMAT_PLAIN,
    FORMAT_IMAGE,
    FORMAT_BINARY
}