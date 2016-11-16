package com.icapps.niddler.ui.model.ui

import com.icapps.niddler.ui.model.ParsedNiddlerMessageSubItem
import javax.swing.tree.DefaultMutableTreeNode

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerMessageSubItemTreeNode(val item: ParsedNiddlerMessageSubItem) : DefaultMutableTreeNode() {
    override fun toString(): String {
        return item.name
    }
}