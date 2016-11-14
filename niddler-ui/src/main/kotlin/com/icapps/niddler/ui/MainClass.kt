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
    window.init()
}
