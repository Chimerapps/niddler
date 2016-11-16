package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.NiddlerClient
import com.icapps.niddler.ui.NiddlerClientListener
import com.icapps.niddler.ui.adb.ADBBootstrap
import com.icapps.niddler.ui.model.*
import com.icapps.niddler.ui.util.getStatusCodeString
import se.vidstige.jadb.JadbDevice
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JFrame
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
        windowContents.buttonClear.addActionListener {
            messages.clear()
            windowContents.dummyContentPanel.text = ""
            // TODO uncomment windowContents.dummyContentPanel.removeAll()
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
        windowContents.dummyContentPanel.text = ""
        val device = devices.find { it.serial == selectedSerial }
        adbConnection.extend(device)?.fowardTCPPort(6555, 6555)
        niddlerClient = NiddlerClient(URI.create("ws://127.0.0.1:6555"))
        niddlerClient?.registerClientListener(this)
        niddlerClient?.registerMessageListener(messages)
        niddlerClient?.connectBlocking()
    }

    private fun showMessageDetails(message: ParsedNiddlerMessage) {
        windowContents.detailPanel.removeAll()
        if (message.bodyFormat.type == BodyFormatType.FORMAT_JSON) {
            windowContents.detailPanel.minimumSize = Dimension(100, 0)
            windowContents.detailPanel.add(NiddlerJsonDataPanel(message), BorderLayout.CENTER)
        } else if (message.bodyFormat.type == BodyFormatType.FORMAT_XML) {
            windowContents.detailPanel.minimumSize = Dimension(100, 0)
            windowContents.detailPanel.add(NiddlerXMLDataPanel(message), BorderLayout.CENTER)
        }
        windowContents.detailPanel.revalidate()
        windowContents.detailPanel.repaint()
    }

    override fun onConnected() {
        val timestamp = ZonedDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

        SwingUtilities.invokeLater {
            windowContents.dummyContentPanel.append("${timestamp.format(formatter)}: Connected to ${niddlerClient?.connection?.remoteSocketAddress}\n\n")
        }
    }

    override fun onMessage(message: ParsedNiddlerMessage) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
        val timestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(message.timestamp), Clock.systemDefaultZone().zone)

        SwingUtilities.invokeLater {
            if (message.isRequest) {
                windowContents.dummyContentPanel.append("${timestamp.format(formatter)}: REQ  ${message.requestId} | ${message.method} ${message.url}")
            } else {
                if (message.body != null) {
                    windowContents.dummyContentPanel.append("${timestamp.format(formatter)}: RESP ${message.requestId} | ${message.statusCode} ${getStatusCodeString(message.statusCode!!)}\nHeaders: ${message.headers}\nBody:${message.bodyFormat.type};")
                    showMessageDetails(message)
                } else {
                    windowContents.dummyContentPanel.append("${timestamp.format(formatter)}: RESP ${message.requestId} | ${message.statusCode} ${getStatusCodeString(message.statusCode!!)}\nHeaders: ${message.headers}\nBody: -NO BODY-")
                }
            }
            windowContents.dummyContentPanel.append("\n\n")

// TODO UNCOMMENT VOOR JTREE
//            val model = windowContents.dummyContentPanel.model as DefaultTreeModel
//            val rootNode = (model.root as DefaultMutableTreeNode)
//
//            val node = NiddlerMessageTreeNode(message)
//            rootNode.add(node)
//            model.reload()

        }
    }

}