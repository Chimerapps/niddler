package com.icapps.niddler.ui.model.ui.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.icapps.niddler.ui.asEnumeration
import java.math.BigInteger
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
        return children.iterator().asEnumeration()
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
            Type.ARRAY -> if (name != null) "$name[$childCount]" else "array[$childCount]"
            Type.OBJECT -> name ?: "object"
            Type.PRIMITIVE -> if (name != null) "$name : $value" else "$value"
            else -> ""
        }
    }

    fun actualType(): JsonDataType {
        return when (type) {
            Type.ARRAY -> JsonDataType.ARRAY
            Type.OBJECT -> JsonDataType.OBJECT
            Type.PRIMITIVE -> {
                if (jsonElement.isJsonNull)
                    return JsonDataType.NULL

                val primitive = jsonElement.asJsonPrimitive
                if (primitive.isBoolean)
                    return JsonDataType.BOOLEAN
                else if (primitive.isString)
                    return JsonDataType.STRING

                val number = primitive.asNumber
                if (number is BigInteger || number is Long || number is Int
                        || number is Short || number is Byte)
                    return JsonDataType.INT
                return JsonDataType.DOUBLE
            }
        }
    }

    fun isAnonymous(): Boolean {
        return name == null
    }

    private enum class Type {
        ARRAY, OBJECT, PRIMITIVE
    }

    enum class JsonDataType {
        ARRAY, OBJECT, BOOLEAN, INT, STRING, DOUBLE, NULL
    }

}