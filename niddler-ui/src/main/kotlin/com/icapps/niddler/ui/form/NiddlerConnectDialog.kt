package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.addChangeListener
import se.vidstige.jadb.JadbConnection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.ListModel

/**
 * @author Nicola Verbeeck
 * @date 17/11/16.
 */
class NiddlerConnectDialog(parent: JFrame?, val adbConnection: JadbConnection, val previousIp: String?, val previousPort: Int?) : ConnectDialog(parent) {

    companion object {
        @JvmStatic
        fun showDialog(parent: JFrame?, adbConnection: JadbConnection, previousIp: String?, previousPort: Int?): ConnectSelection? {
            val dialog = NiddlerConnectDialog(parent, adbConnection, previousIp, previousPort)
            dialog.initUI()
            dialog.pack()
            if (parent != null)
                dialog.setLocationRelativeTo(parent)
            dialog.isVisible = true
            return dialog.selection
        }
    }

    private var selection: ConnectSelection? = null

    private fun initUI() {
        val model = DefaultListModel<String>()
        adbConnection.devices.forEach { model.addElement(it.serial) }

        port.addActionListener { onOK() }
        directIP.addActionListener { onOK() }

        @Suppress("UNCHECKED_CAST")
        adbList.model = model as ListModel<Any>

        if (previousIp != null)
            directIP.text = previousIp
        if (previousPort != null)
            port.text = previousPort.toString()

        adbList.addListSelectionListener {
            if (!adbList.isSelectionEmpty)
                directIP.text = ""
        }
        directIP.addChangeListener {
            if (!directIP.text.isNullOrBlank())
                adbList.clearSelection()
        }

        adbList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2)
                    onOK()
                else
                    super.mouseClicked(e)
            }
        })
    }

    override fun onOK() {
        if (!validateContents())
            return

        selection = ConnectSelection(if (adbList.isSelectionEmpty) null else adbList.selectedValue as String, directIP.text, port.text.toInt())

        dispose()
    }

    override fun onCancel() {
        dispose()
    }

    private fun validateContents(): Boolean {
        try {
            val int = port.text.toInt()
            if (int <= 0 || int > 65535) {
                return showError("Please enter a valid port number")
            }
        } catch(e: NumberFormatException) {
            return showError("Please enter a valid port")
        }
        if (!adbList.isSelectionEmpty)
            return true

        if (directIP.text.isNullOrBlank())
            return showError("Please select a device or enter an ip address")
        return true
    }

    private fun showError(error: String): Boolean {
        JOptionPane.showMessageDialog(this, error, "Could not connect", JOptionPane.ERROR_MESSAGE)
        return false
    }

    data class ConnectSelection(val serial: String?, val ip: String?, val port: Int)

}