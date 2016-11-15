package com.icapps.niddler.ui.model.ui

import java.awt.Component
import java.awt.Font
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JTree
import javax.swing.tree.DefaultTreeCellRenderer

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class JsonTreeRenderer : DefaultTreeCellRenderer() {

    private val stringIcon: Icon
    private var italicFont: Font
    private var regularFont: Font

    init {
        stringIcon = ImageIcon(javaClass.getResource("/ic_string.png"))
        italicFont = Font("Monospaced", Font.ITALIC, 11)
        regularFont = Font("Monospaced", 0, 11)
    }

    override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

        icon = null
        font = regularFont
        if (value is JsonTreeNode)
            if (value.isString())
                icon = stringIcon
            else if (value.isAnonymousObject() || value.isArray())
                font = italicFont

        return this
    }

}