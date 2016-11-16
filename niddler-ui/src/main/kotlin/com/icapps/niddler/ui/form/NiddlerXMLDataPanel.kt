package com.icapps.niddler.ui.form

import com.google.gson.GsonBuilder
import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.model.ui.xml.XMLTreeNode
import javax.swing.JTree
import javax.swing.text.Document
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerXMLDataPanel(message: ParsedNiddlerMessage) : NiddlerStructuredDataPanel(message) {

    init {
        initUI()
    }

    override fun createTreeView() {
        treeView = JTree()
        treeView.isEditable = false
        treeView.showsRootHandles = true
        treeView.isRootVisible = true
        treeView.model = DefaultTreeModel(XMLTreeNode((message.bodyData as org.w3c.dom.Document).documentElement, null), false)

//        treeView.cellRenderer = JsonTreeRenderer()
        treeView.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
    }

    override fun createPrettyPrintedView(doc: Document) {
        doc.remove(0, doc.length)
        doc.insertString(0, GsonBuilder().setPrettyPrinting().create().toJson(message.bodyData), null)
    }

}