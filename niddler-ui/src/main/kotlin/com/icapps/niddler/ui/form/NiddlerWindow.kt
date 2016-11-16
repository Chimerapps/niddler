package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.NiddlerClient
import com.icapps.niddler.ui.NiddlerClientListener
import com.icapps.niddler.ui.adb.ADBBootstrap
import com.icapps.niddler.ui.model.*
import com.icapps.niddler.ui.model.ui.NiddlerMessageTreeNode
import com.icapps.niddler.ui.model.ui.NiddlerTreeRenderer
import se.vidstige.jadb.JadbDevice
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.net.URI
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

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
        windowContents.messagesTree.model = DefaultTreeModel(DefaultMutableTreeNode("messages"))
        windowContents.messagesTree.cellRenderer = NiddlerTreeRenderer()
        windowContents.buttonClear.addActionListener {
            messages.clear()
            val model = windowContents.messagesTree.model as DefaultTreeModel
            (model.root as DefaultMutableTreeNode).removeAllChildren()
            model.reload()
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
        //TODO ?
    }

    override fun onMessage(message: ParsedNiddlerMessage) {
        SwingUtilities.invokeLater {
            if (!message.isRequest) {
                if (message.body != null) {
                    showMessageDetails(message)
                }
            }

            val model = windowContents.messagesTree.model as DefaultTreeModel
            val rootNode = (model.root as DefaultMutableTreeNode)

            val node = NiddlerMessageTreeNode(message)
            rootNode.add(node)
            model.reload()
        }
    }

}