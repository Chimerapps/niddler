package com.icapps.niddler.ui.form

import com.google.gson.JsonElement
import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.model.ui.JsonTreeNode
import com.icapps.niddler.ui.model.ui.JsonTreeRenderer
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerJsonPanel(val message: ParsedNiddlerMessage) : JPanel() {

    private var treeView: JTree

    init {
        layout = BorderLayout()

        treeView = JTree()
        treeView.isEditable = false
        treeView.showsRootHandles = true
        treeView.isRootVisible = true
        treeView.model = DefaultTreeModel(JsonTreeNode(message.bodyData as JsonElement, null, null), false)
        treeView.cellRenderer = JsonTreeRenderer()
        treeView.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

        add(JScrollPane(treeView), BorderLayout.CENTER)
    }

}