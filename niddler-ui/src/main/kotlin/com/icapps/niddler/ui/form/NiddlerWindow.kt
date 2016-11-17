package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.NiddlerClient
import com.icapps.niddler.ui.NiddlerClientListener
import com.icapps.niddler.ui.adb.ADBBootstrap
import com.icapps.niddler.ui.model.*
import com.icapps.niddler.ui.model.ui.TimelineMessagesTableModel
import com.icapps.niddler.ui.setColumnFixedWidth
import se.vidstige.jadb.JadbDevice
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.net.URI
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.SwingUtilities

/**
 * @author Nicola Verbeeck
 * @date 14/11/16.
 */
class NiddlerWindow : JFrame(), NiddlerClientListener, NiddlerMessageListener {

    private val windowContents = MainWindow()
    private val adbConnection = ADBBootstrap()

    private lateinit var devices: MutableList<JadbDevice>
    private var selectedSerial: String? = null
    private val messages = MessageContainer(NiddlerMessageBodyParser())

    fun init() {
        add(windowContents.rootPanel)
        devices = adbConnection.bootStrap().devices

        devices.forEach {
            windowContents.adbTargetSelection.addItem(it.serial)
        }
        windowContents.adbTargetSelection.addActionListener {
            onDeviceSelectionChanged()
        }
        windowContents.messages.model = TimelineMessagesTableModel()
        windowContents.messages.setColumnFixedWidth(0, 90)
        windowContents.messages.setColumnFixedWidth(1, 36)
        windowContents.messages.setColumnFixedWidth(2, 50)

        windowContents.messages.selectionModel.addListSelectionListener {
            if (windowContents.messages.selectedRowCount == 0) {
                clearDetailPanel()
            } else {
                val selectedRow = windowContents.messages.selectedRow
                val row = (windowContents.messages.model as TimelineMessagesTableModel).getRow(selectedRow)
                showMessageDetails(row)
            }
        }

        clearDetailPanel()
        windowContents.buttonClear.addActionListener {
            messages.clear()
            val model = windowContents.messages.model as TimelineMessagesTableModel
            windowContents.messages.clearSelection()
            model.updateMessages(messages)
        }

        pack()
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                super.windowClosing(e)
                messages.unregisterListener(this@NiddlerWindow)
            }

            override fun windowOpened(e: WindowEvent?) {
                super.windowOpened(e)
                messages.registerListener(this@NiddlerWindow)
            }
        })
        isVisible = true
        onDeviceSelectionChanged()
    }

    private fun onDeviceSelectionChanged() {
        if (windowContents.adbTargetSelection.selectedItem == null) {
            return
        }
        selectedSerial = windowContents.adbTargetSelection.selectedItem.toString()
        initNiddlerOnDevice()
    }

    private var niddlerClient: NiddlerClient? = null

    private fun initNiddlerOnDevice() {
        niddlerClient?.close()
        niddlerClient?.unregisterClientListener(this)
        niddlerClient?.unregisterMessageListener(messages)
        messages.clear()
        if (niddlerClient != null) {
            //TODO Remove previous port mapping
        }
        val device = devices.find { it.serial == selectedSerial }
        adbConnection.extend(device)?.fowardTCPPort(6555, 6555)
        niddlerClient = NiddlerClient(URI.create("ws://127.0.0.1:6555"))
        niddlerClient?.registerClientListener(this)
        niddlerClient?.registerMessageListener(messages)
        niddlerClient?.connectBlocking()
    }

    private fun clearDetailPanel() {
        windowContents.detailPanel.removeAll()
        windowContents.detailPanel.add(JLabel("Select a request/response to the body", SwingConstants.CENTER), BorderLayout.CENTER)
        windowContents.detailPanel.revalidate()
        windowContents.splitPane.revalidate()
        windowContents.detailPanel.repaint()
    }

    private fun showEmptyMessageDetails() {
        windowContents.detailPanel.removeAll()
        windowContents.detailPanel.add(JLabel("This request/response has no body", SwingConstants.CENTER), BorderLayout.CENTER)
        windowContents.detailPanel.revalidate()
        windowContents.splitPane.revalidate()
        windowContents.detailPanel.repaint()
    }

    private fun showMessageDetails(message: ParsedNiddlerMessage) {
        windowContents.detailPanel.removeAll()
        if (message.bodyFormat.type == BodyFormatType.FORMAT_JSON) {
            windowContents.detailPanel.add(NiddlerJsonDataPanel(message), BorderLayout.CENTER)
        } else if (message.bodyFormat.type == BodyFormatType.FORMAT_XML) {
            windowContents.detailPanel.add(NiddlerXMLDataPanel(message), BorderLayout.CENTER)
        } else if (message.body.isNullOrBlank()) {
            showEmptyMessageDetails()
            return
        }
        windowContents.splitPane.revalidate()
        windowContents.detailPanel.revalidate()
        windowContents.detailPanel.repaint()
    }

    override fun onConnected() {
        //TODO ?
    }

    override fun onMessage(message: ParsedNiddlerMessage) {
        SwingUtilities.invokeLater {
            val previousSelection = windowContents.messages.selectedRow
            (windowContents.messages.model as TimelineMessagesTableModel).updateMessages(messages)
            if (previousSelection != -1)
                windowContents.messages.addRowSelectionInterval(previousSelection,previousSelection)
        }
    }

}