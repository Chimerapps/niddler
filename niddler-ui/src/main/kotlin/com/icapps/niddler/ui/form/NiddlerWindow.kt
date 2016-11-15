package com.icapps.niddler.ui.form

import com.google.gson.Gson
import com.icapps.niddler.ui.NiddlerClient
import com.icapps.niddler.ui.NiddlerClientListener
import com.icapps.niddler.ui.adb.ADBBootstrap
import com.icapps.niddler.ui.model.NiddlerMessage
import se.vidstige.jadb.JadbDevice
import java.net.URI
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JFrame
import javax.swing.SwingUtilities

/**
 * @author Nicola Verbeeck
 * @date 14/11/16.
 */
class NiddlerWindow : JFrame(), NiddlerClientListener {

    private val windowContents = MainWindow()
    private val adbConnection = ADBBootstrap()

    private lateinit var devices: MutableList<JadbDevice>
    private var selectedSerial: String? = null

    fun init() {
        windowContents.initImages()
        add(windowContents.rootPanel)
        devices = adbConnection.bootStrap().devices

        devices.forEach {
            windowContents.adbTargetSelection.addItem(it.serial)
        }
        windowContents.adbTargetSelection.addActionListener {
            onDeviceSelectionChanged()
        }

        pack()
        isVisible = true

        onDeviceSelectionChanged()
    }

    private fun onDeviceSelectionChanged() {
        selectedSerial = windowContents.adbTargetSelection.selectedItem.toString()
        initNiddlerOnDevice()
    }

    private var niddlerClient: NiddlerClient? = null

    private fun initNiddlerOnDevice() {
        niddlerClient?.close()
        niddlerClient?.unregisterListener(this)
        if (niddlerClient != null) {
            //TODO Remove previous port mapping
        }
        windowContents.dummyContentPanel.text = ""
        val device = devices.find { it.serial == selectedSerial }
        adbConnection.extend(device)?.fowardTCPPort(6555, 6555)
        niddlerClient = NiddlerClient(URI.create("ws://127.0.0.1:6555"))
        niddlerClient?.registerListener(this)
        niddlerClient?.connectBlocking()
    }

    override fun onConnected() {
        val timestamp = ZonedDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

        SwingUtilities.invokeLater {
            windowContents.dummyContentPanel.append("${timestamp.format(formatter)}: Connected to ${niddlerClient?.connection?.remoteSocketAddress}\n\n")
        }
    }

    override fun onMessage(msg: String) {
        val timestamp = ZonedDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
        val message = Gson().fromJson(msg, NiddlerMessage::class.java)

        SwingUtilities.invokeLater {
            if (message.isRequest) {
                windowContents.dummyContentPanel.append("${timestamp.format(formatter)}: REQ  ${message.requestId} | ${message.method} ${message.url}\n")
            } else {
                if (message.body != null) {
                    windowContents.dummyContentPanel.append("${timestamp.format(formatter)}: RESP ${message.requestId} | ${message.statusCode} ${message.headers} ${message.getBodyAsString}\n")
                } else {
                    windowContents.dummyContentPanel.append("${timestamp.format(formatter)}: RESP ${message.requestId} | ${message.statusCode} ${message.headers} - No body\n")
                }

            }

            windowContents.dummyContentPanel.append("\n")
        }
    }

}