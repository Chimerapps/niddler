package com.icapps.niddler.ui.model.ui

import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerMessageTreeNode(val item: ParsedNiddlerMessage) : DefaultMutableTreeNode() {

    private val children: MutableList<TreeNode> = arrayListOf()

    init {
        if (!item.isRequest) {
            initResponseSubitems(item)
        }
    }

    private fun initResponseSubitems(item: ParsedNiddlerMessage) {
        item.subItems.forEach {
            add(NiddlerMessageSubItemTreeNode(it))
        }
    }

}