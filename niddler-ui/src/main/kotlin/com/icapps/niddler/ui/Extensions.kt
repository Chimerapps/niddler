package com.icapps.niddler.ui

import java.util.*

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
public fun String.prefixList(elements: Array<out String>): List<String> {
    val list = ArrayList<String>(elements.size + 1)
    list.add(this)
    list.addAll(elements)
    return list
}