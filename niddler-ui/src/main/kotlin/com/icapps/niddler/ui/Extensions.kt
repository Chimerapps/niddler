package com.icapps.niddler.ui

import java.util.*
import javax.swing.JTable

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
fun String.prefixList(elements: Array<out String>): List<String> {
    val list = ArrayList<String>(elements.size + 1)
    list.add(this)
    list.addAll(elements)
    return list
}

fun nop() {
}

fun <T> Iterator<T>.asEnumeration(): Enumeration<T> {
    return object : Enumeration<T> {
        override fun hasMoreElements(): Boolean {
            return hasNext()
        }

        override fun nextElement(): T {
            return next()
        }

    }
}

fun JTable.setColumnFixedWidth(columnIndex: Int, width: Int) {
    val column = columnModel.getColumn(columnIndex)
    column?.minWidth = width
    column?.maxWidth = width
    column?.preferredWidth = width
}