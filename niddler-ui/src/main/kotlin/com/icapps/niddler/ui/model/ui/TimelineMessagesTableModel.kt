package com.icapps.niddler.ui.model.ui

import com.icapps.niddler.ui.model.MessageContainer
import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.util.getStatusCodeString
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

/**
 * @author Nicola Verbeeck
 * @date 17/11/16.
 */
class TimelineMessagesTableModel : TableModel {

    companion object {
        @JvmStatic private val INDEX_TIMESTAMP = 0
        @JvmStatic private val INDEX_DIRECTION = 1
        @JvmStatic private val INDEX_METHOD = 2
        @JvmStatic private val INDEX_URL = 3
        @JvmStatic private val INDEX_STATUS_CODE = 4
        @JvmStatic private val INDEX_FORMAT = 5
    }

    private val listeners: MutableSet<TableModelListener> = hashSetOf()
    private var messages: List<ParsedNiddlerMessage> = Collections.emptyList()
    private val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private val upIcon: Icon
    private val downIcon: Icon
    private lateinit var container: MessageContainer

    init {
        upIcon = ImageIcon(javaClass.getResource("/ic_up.png"))
        downIcon = ImageIcon(javaClass.getResource("/ic_down.png"))
    }

    fun updateMessages(msgs: MessageContainer) {
        messages = msgs.getMessagesChronological()
        container = msgs

        val e = TableModelEvent(this)
        listeners.forEach { it.tableChanged(e) }
    }

    override fun addTableModelListener(l: TableModelListener) {
        listeners.add(l)
    }

    override fun getRowCount(): Int {
        return messages.size
    }

    override fun getColumnName(columnIndex: Int): String {
        return when (columnIndex) {
            INDEX_TIMESTAMP -> "Timestamp"
            INDEX_DIRECTION -> "Up/Down"
            INDEX_METHOD -> "Method"
            INDEX_URL -> "Url"
            INDEX_STATUS_CODE -> "Status"
            INDEX_FORMAT -> "Format"
            else -> "<NO COLUMN NAME>"
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return when (columnIndex) {
            INDEX_TIMESTAMP, INDEX_URL, INDEX_METHOD -> String::class.java
            INDEX_DIRECTION -> Icon::class.java
            INDEX_STATUS_CODE -> String::class.java
            INDEX_FORMAT -> String::class.java
            else -> String::class.java
        }
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        //Not allowed
    }

    override fun getColumnCount(): Int {
        return 6
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        val message = messages[rowIndex]
        val other = if (message.isRequest) findResponse(message) else findRequest(message)

        return when (columnIndex) {
            INDEX_TIMESTAMP -> formatter.format(Date(message.timestamp))
            INDEX_DIRECTION -> if (message.isRequest) upIcon else downIcon
            INDEX_METHOD -> message.method ?: other?.method
            INDEX_URL -> message.url ?: other?.url
            INDEX_STATUS_CODE -> if (message.statusCode != null) formatStatusCode(message.statusCode) else formatStatusCode(other?.statusCode)
            INDEX_FORMAT -> message.bodyFormat
            else -> "<NO COLUMN DEF>"
        }
    }

    private fun formatStatusCode(statusCode: Int?): String {
        return if (statusCode == null) {
            ""
        } else {
            String.format("%d %s", statusCode, getStatusCodeString(statusCode))
        }
    }

    override fun removeTableModelListener(l: TableModelListener) {
        listeners.remove(l)
    }

    fun getRow(selectedRow: Int): ParsedNiddlerMessage {
        return messages[selectedRow]
    }

    private fun findResponse(message: ParsedNiddlerMessage): ParsedNiddlerMessage? {
        return container.getMessagesWithRequestId(message.requestId)?.find {
            !it.isRequest
        }
    }

    private fun findRequest(message: ParsedNiddlerMessage): ParsedNiddlerMessage? {
        return container.getMessagesWithRequestId(message.requestId)?.find(ParsedNiddlerMessage::isRequest)
    }

}