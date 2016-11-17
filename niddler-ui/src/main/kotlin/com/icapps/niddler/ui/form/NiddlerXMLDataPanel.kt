package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.model.ui.json.XMLTreeRenderer
import com.icapps.niddler.ui.model.ui.xml.XMLTreeNode
import java.io.StringWriter
import javax.swing.JTree
import javax.swing.text.Document
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

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

        treeView.cellRenderer = XMLTreeRenderer()
        treeView.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
    }

    override fun createPrettyPrintedView(doc: Document) {
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        transformer.setOutputProperty(OutputKeys.METHOD, "xml")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.ENCODING, message.bodyFormat.encoding ?: "UTF-8")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

        val writer = StringWriter()
        transformer.transform(DOMSource(message.bodyData as org.w3c.dom.Document?), StreamResult(writer))

        doc.remove(0, doc.length)
        doc.insertString(0, writer.toString(), null)
    }

}