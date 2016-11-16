package com.icapps.niddler.ui.model.ui

import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import javax.swing.tree.DefaultMutableTreeNode

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerMessageTreeNode(val item: ParsedNiddlerMessage) : DefaultMutableTreeNode() {

    init {
        initResponseSubItems(item)
    }

    private fun initResponseSubItems(item: ParsedNiddlerMessage) {
        item.subItems.forEach {
            add(NiddlerMessageSubItemTreeNode(it))
        }
    }

    override fun toString(): String {
        return item.messageId
    }

}