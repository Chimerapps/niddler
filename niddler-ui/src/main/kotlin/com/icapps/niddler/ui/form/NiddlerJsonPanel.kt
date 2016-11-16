package com.icapps.niddler.ui.form

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.icapps.niddler.ui.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.model.ui.JsonTreeNode
import com.icapps.niddler.ui.model.ui.JsonTreeRenderer
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerJsonPanel(val message: ParsedNiddlerMessage) : JPanel() {

    private var currentContentPanel: JComponent? = null

    private lateinit var treeView: JTree

    private val treeButton: JToggleButton
    private val prettyButton: JToggleButton
    private val rawButton: JToggleButton

    init {
        layout = BorderLayout()

        treeButton = JToggleButton("Structure", ImageIcon(javaClass.getResource("/ic_as_tree.png")))
        prettyButton = JToggleButton("Pretty", ImageIcon(javaClass.getResource("/ic_as_tree.png")))
        rawButton = JToggleButton("Raw", ImageIcon(javaClass.getResource("/ic_as_tree.png")))

        val buttonGroup = ButtonGroup()
        buttonGroup.add(treeButton)
        buttonGroup.add(prettyButton)
        buttonGroup.add(rawButton)

        val toolbar = JToolBar()
        toolbar.isFloatable = false
        toolbar.add(Box.createGlue())
        toolbar.add(treeButton)
        toolbar.add(prettyButton)
        toolbar.add(rawButton)

        add(toolbar, BorderLayout.NORTH)

        treeButton.isSelected = true
        preInitTree()
        initAsTree()

        treeButton.addItemListener { if (treeButton.isSelected) initAsTree() }
        prettyButton.addItemListener { if (prettyButton.isSelected) initAsPretty() }
        rawButton.addItemListener { if (rawButton.isSelected) initAsRaw() }
    }


    private fun preInitTree() {
        treeView = JTree()
        treeView.isEditable = false
        treeView.showsRootHandles = true
        treeView.isRootVisible = true
        treeView.model = DefaultTreeModel(JsonTreeNode(message.bodyData as JsonElement, null, null), false)

        treeView.cellRenderer = JsonTreeRenderer()
        treeView.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
    }

    private fun initAsTree(){
        replacePanel(JScrollPane(treeView))
    }

    private fun initAsPretty() {
        val textArea = JTextArea()
        textArea.text = GsonBuilder().setPrettyPrinting().create().toJson(message.bodyData)
        textArea.isEditable = false
        replacePanel(JScrollPane(textArea))
    }

    private fun initAsRaw() {
        val textArea = JTextArea()
        textArea.text = message.message.getBodyAsString(message.bodyFormat.encoding)
        textArea.isEditable = false
        replacePanel(JScrollPane(textArea))
    }

    private fun replacePanel(newContents: JComponent) {
        if (currentContentPanel != null) remove(currentContentPanel)
        add(newContents, BorderLayout.CENTER)
        currentContentPanel = newContents
        revalidate()
        repaint()
    }

}