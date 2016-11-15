package com.icapps.niddler.ui.model.ui

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.util.*
import javax.swing.tree.TreeNode

/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class JsonTreeNode(private val jsonElement: JsonElement, private val parent: TreeNode?, private val name: String?) : TreeNode {

    private val children: MutableList<JsonTreeNode> = arrayListOf()
    private var value: String? = null
    private var type: Type = Type.PRIMITIVE

    init {
        if (jsonElement.isJsonArray)
            populateFromArray(jsonElement.asJsonArray)
        else if (jsonElement.isJsonObject)
            populateFromObject(jsonElement.asJsonObject)
        else if (jsonElement.isJsonNull)
            initLeaf("null")
        else
            initLeafPrimitive(jsonElement.asJsonPrimitive)
    }

    private fun populateFromArray(jsonArray: JsonArray) {
        jsonArray.forEach {
            children.add(JsonTreeNode(it, this, null))
        }
        type = Type.ARRAY
    }

    private fun populateFromObject(jsonObject: JsonObject) {
        jsonObject.entrySet().forEach {
            children.add(JsonTreeNode(it.value, this, it.key))
        }
        type = Type.OBJECT
    }

    private fun initLeaf(valueAsString: String) {
        value = valueAsString
    }

    private fun initLeafPrimitive(jsonPrimitive: JsonPrimitive) {
        value = jsonPrimitive.toString()
    }

    override fun children(): Enumeration<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isLeaf(): Boolean {
        return children.isEmpty()
    }

    override fun getChildCount(): Int {
        return children.size
    }

    override fun getParent(): TreeNode? {
        return parent
    }

    override fun getChildAt(childIndex: Int): TreeNode {
        return children[childIndex]
    }

    override fun getIndex(node: TreeNode?): Int {
        return children.indexOf(node)
    }

    override fun getAllowsChildren(): Boolean {
        return true //No idea?
    }

    override fun toString(): String {
        return when (type) {
            JsonTreeNode.Type.ARRAY -> "array[$childCount]"
            JsonTreeNode.Type.OBJECT -> name ?: "object"
            JsonTreeNode.Type.PRIMITIVE -> if (name != null) "$name : $value" else "$value"
            else -> ""
        }
    }

    fun isString(): Boolean {
        return type == Type.PRIMITIVE && (jsonElement.asJsonPrimitive.isString)
    }

    fun isAnonymousObject() : Boolean {
        return type == Type.OBJECT && name == null
    }

    fun isArray():Boolean{
        return type == Type.ARRAY
    }

    private enum class Type {
        ARRAY, OBJECT, PRIMITIVE
    }

}