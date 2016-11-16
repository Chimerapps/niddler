package com.icapps.niddler.ui.model.ui

import com.icapps.niddler.ui.util.getStatusCodeString
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.tree.DefaultTreeCellRenderer

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerTreeRenderer : DefaultTreeCellRenderer() {

    private val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private val timestampLabel = JLabel("")
    private val urlLabel = JLabel("")
    private val methodLabel = JLabel("")
    private val statusCodeLabel = JLabel("")
    private val nameLabel = JLabel("")
    private val valueLabel = JLabel("")
    private val directionIconLabel = JLabel()
    private val upIcon: Icon
    private val downIcon: Icon

    private val rootPanel = JPanel()
    private val defaultRenderer = DefaultTreeCellRenderer()

    init {
        timestampLabel.border = EmptyBorder(0, 0, 0, 15)
        timestampLabel.font = Font("SansSerif", Font.PLAIN, 11)
        timestampLabel.background = Color.RED
        rootPanel.add(timestampLabel)

        directionIconLabel.border = EmptyBorder(0, 0, 0, 5)
        rootPanel.add(directionIconLabel)

        methodLabel.border = EmptyBorder(0, 0, 0, 5)
        methodLabel.font = Font("Monospaced", Font.PLAIN, 11)
        rootPanel.add(methodLabel)

        urlLabel.border = EmptyBorder(0, 0, 0, 10)
        urlLabel.font = Font("Monospaced", Font.PLAIN, 11)
        rootPanel.add(urlLabel)

        statusCodeLabel.border = EmptyBorder(0, 0, 0, 10)
        statusCodeLabel.font = Font("Monospaced", Font.PLAIN, 11)
        rootPanel.add(statusCodeLabel)

        nameLabel.preferredSize = Dimension(63, 15)
        nameLabel.border = EmptyBorder(0, 0, 0, 10)
        nameLabel.font = Font("SansSerif", Font.BOLD, 11)
        rootPanel.add(nameLabel)

        valueLabel.border = EmptyBorder(0, 0, 0, 10)
        valueLabel.font = Font("Monospaced", Font.PLAIN, 11)

        upIcon = ImageIcon(javaClass.getResource("/ic_up.png"))
        downIcon = ImageIcon(javaClass.getResource("/ic_down.png"))

        rootPanel.isOpaque = false
        rootPanel.add(valueLabel)
    }

    override fun getTreeCellRendererComponent(tree: JTree, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        if (value is NiddlerMessageTreeNode) {
            nameLabel.isVisible = false
            valueLabel.isVisible = false
            timestampLabel.isVisible = true
            directionIconLabel.isVisible = true
            urlLabel.isVisible = value.item.isRequest
            methodLabel.isVisible = value.item.isRequest
            statusCodeLabel.isVisible = !value.item.isRequest

            timestampLabel.text = formatter.format(Date(value.item.timestamp))
            urlLabel.text = value.item.url
            methodLabel.text = value.item.method
            //  directionIconLabel.icon = if (value.item.isRequest) upIcon else downIcon
            statusCodeLabel.text = "${value.item.statusCode} ${getStatusCodeString(value.item.statusCode)}"

            return rootPanel
        } else if (value is NiddlerMessageSubItemTreeNode) {
            nameLabel.isVisible = true
            valueLabel.isVisible = true
            urlLabel.isVisible = false
            statusCodeLabel.isVisible = false
            timestampLabel.isVisible = false
            directionIconLabel.isVisible = false

            nameLabel.text = value.item.name + ":"
            valueLabel.text = value.item.value

            return rootPanel
        }

        return defaultRenderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
    }

}