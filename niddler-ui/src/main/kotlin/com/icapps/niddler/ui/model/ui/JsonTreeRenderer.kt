package com.icapps.niddler.ui.model.ui

import java.awt.Component
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JTree
import javax.swing.tree.DefaultTreeCellRenderer

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class JsonTreeRenderer : DefaultTreeCellRenderer() {

    private val stringIcon:Icon

    init{
        stringIcon = ImageIcon(javaClass.getResource("/ic_string.png"))
    }

    override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

        if (value is JsonTreeNode)
            if (value.isString())
                icon = stringIcon
            else
                icon = null
        else
            icon = null

        return this
    }

}