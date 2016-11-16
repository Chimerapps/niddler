package com.icapps.niddler.ui

import com.icapps.niddler.ui.form.NiddlerWindow
import javax.swing.WindowConstants


/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
fun main(args: Array<String>) {
    val window = NiddlerWindow()
    window.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    window.setSize(1000, 600)
    window.init()

//    val f = JFrame("Swag")
//    f.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
//    f.setSize(1200, 800)
//    initializeFWindow(f)
//    f.isVisible = true
}

//fun initializeFWindow(frame: JFrame) {
//    val msg1: NiddlerMessage = NiddlerMessage()
//    msg1.messageId = UUID.randomUUID().toString()
//    msg1.method = "GET"
//    msg1.requestId = UUID.randomUUID().toString()
//    msg1.url = "http://api.maarten.vg/api/v1/helloworld"
//    msg1.timestamp = System.currentTimeMillis()
//
//    val msg2: NiddlerMessage = NiddlerMessage()
//    msg2.requestId = msg1.requestId
//    msg2.messageId = UUID.randomUUID().toString()
//    msg2.timestamp = System.currentTimeMillis()
//    msg2.body = "{\"hello\":\"world\"}"
//    msg2.statusCode = 200
//
//    val item1 = NiddlerMessageItem("headers", "No headers")
//    val item2 = NiddlerMessageItem("body", "hello world, this is the body")
//
//    val rootNode = DefaultMutableTreeNode()
//
//    val msg1Node = DefaultMutableTreeNode(msg1)
//
//    val msg2Node = DefaultMutableTreeNode(msg2)
//    msg2Node.add(DefaultMutableTreeNode(item1))
//    msg2Node.add(DefaultMutableTreeNode(item2))
//
//    rootNode.add(msg1Node)
//    rootNode.add(msg2Node)
//
//    val tree = object : JTree(rootNode) {
//        override fun updateUI() {
//            setCellRenderer(null)
//            super.updateUI()
//            setCellRenderer(CustomJTreeRenderer())
//            setRowHeight(0)
//            isRootVisible = false
//            setShowsRootHandles(false)
//        }
//    }
//
//    frame.contentPane.add(tree)
//}
