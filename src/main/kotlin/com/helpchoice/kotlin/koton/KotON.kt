package com.helpchoice.kotlin.koton

import java.io.*
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

/**
 * This was inspired by project https://github.com/Jire/KTON.
 *
 * Here the content get spread into class hierarchy which helps to scope functions
 * and doesn't require both Map and Array for each instance.
 * As a result access to the value requires extra parenthesis.
 *
 * Also I added toJson() function to convert the Object into (you guessed it) JSON.
 */
sealed class KotON<V : Any>() {
    /**
     * Function to write *this* as JSON into provided @writer.
     *
     * By default this function produces compact JSON. Providing values for parameters
     * allows to form different variations of pretty JSON.
     *
     * @param [writer] -- the Writer to print JSON into
     * @param [separator] -- the String to print before each element
     * @param [increment] -- the String to append to @separator when diving into sub-element
     * @return the @writer to chain conversion to String or continue printing
     */
    open fun toJson(writer: Writer, separator: String = "", increment: String = ""): Writer {
        writer.write("$separator{}")
        return writer
    }

    /**
     * Function to create String containing JSON.
     *
     * By default this function produces compact JSON. Providing values for parameters
     * allows to form different variations of pretty JSON.
     *
     * @param separator -- the String to print before each element
     * @param increment -- the String to append to @separator when diving into sub-element
     * @return String containg JSON representing this object
     */
    fun toJson(separator: String = "", increment: String = ""): String {
        return toJson(StringWriter(), separator, increment).toString()
    }

    protected abstract fun internalValue(): V?

    /**
     * Validates if element for provided keys exists.
     *
     * Dereference operator (**`[`**, **`]`**) will throw execpsion if element doesn't exist.
     * To avoid this check this function first OR use invocation operator (**`(`**, **`)`**).
     */
    fun contains(index: String, vararg key: String): Boolean {
        try {
            this.get(index, *key)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * Returns the number of elements on the top level
     *
     * @return the number of elements on the top level
     * @throws IllegalAccessException if found element of not expected type
     */
    fun size(): Int {
        val value = internalValue()
        return when (value) {
            is Collection<*> -> value.size
            is Dictionary<*, *> -> (value as Map<String, Any?>).entries.size
            is Map<*, *> -> (value as Map<String, Any?>).entries.size
            else -> 1
        }
    }

    /**
     * Retrieves the internal structure value for the path.
     *
     * Each element in the list of parameters represent the relationship to the sub-element to select.
     *
     * If sub-element is a list the parameter String value should be convertible into Int select element from the Collection.
     *
     * @param index -- the top-level relation to look for
     * @param key -- path to navigate deeper into sub-tree
     * @return the sub-element found for the path or `null`
     * @throws NullPointerException if provided relation wasn't found
     * @throws IllegalAccessException if found element of not expected type
     */
    operator fun get(index: String, vararg key: String): KotON<Any> {
        val value = internalValue()
        return when (value) {
            is KotON<*> -> value
            is Collection<*> -> value.toTypedArray()[index.toInt()]
            is Dictionary<*, *> -> (value as Map<String, Any?>)[index]
            is Map<*, *> -> (value as Map<String, Any?>)[index]
            else -> throw IllegalAccessException("Index is not supported by this instance")
        }?.let {
            if (key.isNotEmpty()) {
                when (it) {
                    is KotON<*> -> it.get(key[0], *key.drop(1).toTypedArray())
                    else -> KotONVal(it).get(key[0], *key.drop(1).toTypedArray())
                }
            } else {
                when (it) {
                    is KotON<*> -> it as KotON<Any>
                    else -> KotONVal(it)
                }
            }
        } ?: throw NullPointerException("Object contains no value")
    }

    /**
     * Function retrieves the element from the Collection for provided index
     *
     * @param index -- the top-level numeric index into the Collection
     * @return the sub-element found for the path or `null`
     * @throws NullPointerException if provided relation wasn't found
     * @throws IllegalAccessException if found element of not expected type
     */
    operator fun get(index: Int): KotON<Any> {
        val value = internalValue()
        when (value) {
            is KotON<*> -> value
            is Collection<*> -> value.toTypedArray()[index]
            else -> throw IllegalAccessException("Numeric index is not supported by this instance")
        }?.let {
            return if (it is KotON<*>) {
                it as KotON<Any>
            } else {
                KotONVal(it)
            }
        } ?: throw NullPointerException("Object contains no value")
    }

    /**
     * Dereferences internal value into expected Kotlin type
     *
     * @param T -- the type of the value to extract
     * @param key -- sequence of String values to locate sub-element in the tree
     * @param cls -- required to use type inside. Can be omitted
     * @return dereference value of provided type OR `null`
     */
    open operator fun <T> invoke(vararg key: String, cls: Class<T>? = null): T? {
        try {
            return invoke(*key) as T?
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Dereferences internal value into base type `Any?`
     *
     * @param key -- sequence of String values to locate sub-element in the tree
     * @return dereference value OR `null`
     */
    open operator fun invoke(vararg key: String): Any? {
        try {
            return if (key.isNotEmpty()) {
                get(key[0], *key.drop(1).toTypedArray())()
            } else {
                internalValue()
            }
        } catch (e: Exception) {
            return null
        }
    }
}

private data class KotONVal<V : Any>(val value: V? = null) : KotON<V>() {
    override fun toJson(writer: Writer, separator: String, increment: String): Writer {
        writer.write(
                when (value) {
                    is String -> "\"${value.escape()}\""
                    is Array<*> -> "${value.toList()
                            .map { "${KotONVal(it).toJson("$separator$increment", increment)}" }
                            .joinToString(", ", "$separator[", "$separator]")}"
                    is Collection<*> -> "${value
                            .map { "${KotONVal(it).toJson("$separator$increment", increment)}" }
                            .joinToString(", ", "$separator[", "$separator]")}"
                    is Map<*, *> -> "${value.map { (key, vlue) ->
                        "\"$key\": ${KotONVal(vlue).toJson()}"
                    }.joinToString(",$separator$increment", "$separator{", "$separator}")}"
                    else -> value.toString()
                })
        return writer
    }

    override fun internalValue(): V? {
        return value
    }
}

private data class KotONArray(val value: ArrayList<KotON<Any>> = ArrayList()) : KotON<ArrayList<KotON<Any>>>() {
    override fun toJson(writer: Writer, separator: String, increment: String): Writer {
        value?.joinTo(writer, ",$separator$increment", "[$separator$increment", "$separator]") {
            it.toJson(writer, separator + increment, increment)
            ""
        }
        return writer
    }

    override fun internalValue(): ArrayList<KotON<Any>>? {
        return value
    }

    operator fun plus(body: KotONBuilder.() -> Any): KotONArray {
        value?.let {
            value += kotON(body)
        }
        return this
    }
}

private data class KotONEntry(val content: Map<String, KotON<Any>> = emptyMap()) : KotON<Any>() {
    override fun toJson(writer: Writer, separator: String, increment: String): Writer {
        return (content).entries.joinTo(writer, ",$separator$increment", "{$separator$increment", "$separator}") {
            writer.write("\"${it.key.escape()}\": ")
            it.value.toJson(writer, separator + increment, increment)
            ""
        }
    }

    override fun internalValue(): Map<String, KotON<Any>>? {
        return content
    }
}

data class KotONBuilder(val content: MutableMap<String, KotON<Any>> = mutableMapOf()) {
    infix fun String.to(value: Any) {
        content[this] = KotONVal(value)
    }

    operator fun String.invoke(body: KotONBuilder.() -> Any) {
        content[this] = kotON(body)
    }

    operator fun String.get(vararg bodies: KotONBuilder.() -> Unit) {
        content[this] = kotON(*bodies) as KotON<Any>
    }

    fun build(): KotON<Any> {
        return KotONEntry(content)
    }
}

private fun String.escape(): String {
    return this
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\n", "\\n")
            .replace("\u000C", "\\f")
            .replace("\b", "\\b")
}

fun kotON(value: Any): KotON<Any> {
    return KotONVal(value)
}

/**
 * Top-level DSL function to build the KotON object
 *
 * It builds a KotOn object initialized by provided lambda.
 *
 * @param init -- the lambda to initialize
 * @return the built object
 */
inline fun kotON(init: KotONBuilder.() -> Any): KotON<Any> {
    val root = KotONBuilder()
    root.init()
    return root.build()
}

/**
 * Top-level DSL function to build the KotON object containing the Collection
 *
 * It builds a KotOn objects initialized by provided lambdas and combines them into single Collection.
 *
 * @param bodies -- the lambdas to initialize elements in the Collection
 * @return the built object
 */
fun kotON(vararg bodies: KotONBuilder.() -> Unit): KotON<Any> {
    val kotON = KotONArray()
    bodies.forEach {
        kotON + it
    }
    return kotON as KotON<Any>
}
