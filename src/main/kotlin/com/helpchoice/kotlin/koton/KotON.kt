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
    open fun toJson(writer: Writer, separator: String = "", increment: String = ""): Writer {
        writer.write("$separator{}")
        return writer
    }

    fun toJson(separator: String = "", increment: String = ""): String {
        return toJson(StringWriter(), separator, increment).toString()
    }

    abstract operator fun get(index: String, vararg key: String): KotON<Any>

    protected fun deRef(value: Any?, index: String, vararg key: String): KotON<Any> {
        return when (value) {
            is KotON<*> -> value
            is Collection<*> -> value.toTypedArray()[index.toInt()]
            is Dictionary<*, *> -> value[index]
            else -> throw IllegalAccessException("Index is not supported by this instance")
        }?.let {
            when (it) {
                is KotON<*> -> it.get(key[0], *key.drop(1).toTypedArray())
                else -> KotONVal(it).get(key[0], *key.drop(1).toTypedArray())
            }
        } ?: throw NullPointerException("Object contains no value")
    }

    abstract operator fun get(index: Int): KotON<Any>

    protected fun deRef(value: Any?, index: Int): KotON<Any> {
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

    open operator fun <T> invoke(cls: Class<T>? = null): T? {
        return null
    }

    open operator fun invoke(): Any? {
        return null
    }
}

private data class KotONVal<V : Any>(val value: V? = null) : KotON<V>() {
    override fun toJson(writer: Writer, separator: String, increment: String): Writer {
        writer.write(
                when (value) {
                    is String -> "\"${value.escape()}\""
                    is Array<*> -> "${value.toList()
                            .map { "${KotONVal(it).toJson("$increment$separator", increment)}" }
                            .joinToString(", ", "$separator[", "$separator]")}"
                    is Collection<*> -> "${value
                            .map { "${KotONVal(it).toJson("$increment$separator", increment)}" }
                            .joinToString(", ", "$separator[", "$separator]")}"
                    else -> value.toString()
                })
        return writer
    }

    override operator fun get(index: String, vararg key: String): KotON<Any> {
        return deRef(value, index, *key)
    }

    override operator fun get(index: Int): KotON<Any> {
        return deRef(value, index)
    }

    override operator fun <T> invoke(cls: Class<T>?): T? {
        return value as T
    }

    override operator fun invoke(): Any? {
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

    override operator fun get(index: String, vararg key: String): KotON<Any> {
        return deRef(value, index, *key)
    }

    override operator fun get(index: Int): KotON<Any> {
        return deRef(value, index)
    }

    operator fun plus(body: KotONBuilder.() -> Any): KotONArray {
        value?.let {
            value += kotON(body)
        }
        return this
    }

//    override operator fun get(index: Int): KotON<Any> {
//        return value[index]
//    }
}

private data class KotONEntry(val content: Map<String, KotON<Any>> = emptyMap()) : KotON<Any>() {
    override fun toJson(writer: Writer, separator: String, increment: String): Writer {
        return (content).entries.joinTo(writer, ",$separator$increment", "{$separator$increment", "$separator}") {
            writer.write("\"${it.key.escape()}\": ")
            it.value.toJson(writer, separator + increment, increment)
            ""
        }
    }

    override operator fun get(index: String, vararg key: String): KotON<Any> {
        return (content)[index]?.let {
            if (key.isEmpty()) {
                it
            } else {
                it.get(key[0], *key.drop(1).toTypedArray())
            }
        } ?: throw NullPointerException("Object contains no value")
    }

    override operator fun get(index: Int): KotON<Any> {
        return deRef(content, index)
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

fun String.escape(): String {
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

inline fun kotON(init: KotONBuilder.() -> Any): KotON<Any> {
    val root = KotONBuilder()
    root.init()
    return root.build()
}

fun kotON(vararg bodies: KotONBuilder.() -> Unit): KotON<Any> {
    val kotON = KotONArray()
    bodies.forEach {
        kotON + it
    }
    return kotON as KotON<Any>
}
