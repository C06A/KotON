package com.helpchoice.kotlin.koton

import java.io.*
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

    open operator fun get(index: String, vararg key: String): KotON<Any> {
        return if (key.isNotEmpty()) {
            key.fold(this[index]) { parent, indx ->
                if (parent is KotONEntry) {
                    parent[indx]
                } else {
                    throw IllegalAccessException("Key access is not supported by this instance")
                }
            }
        } else {
            this[index]
        }
    }

    open operator fun get(index: Int): KotON<Any> {
        if (this is KotONArray) {
            return this[index]
        }
        throw IllegalAccessException("Index is not supported by this instance")
    }

    open operator fun <T> invoke(cls: Class<T>? = null): T? {
        return null
    }

    open operator fun invoke(): Any? {
        return null
    }
}

data class KotONVal<V : Any>(val value: V?) : KotON<V>() {
    override fun toJson(writer: Writer, separator: String, increment: String): Writer {
        writer.write(
                when (value) {
                    is String -> "\"${value.escape()}\""
                    else -> value.toString()
                })
        return writer
    }

    override operator fun <T> invoke(cls: Class<T>?): T? {
        return value as T
    }

    override operator fun invoke(): Any? {
        return value
    }
}

data class KotONArray(val value: ArrayList<KotON<Any>> = ArrayList()) : KotON<ArrayList<KotON<Any>>>() {
    override fun toJson(writer: Writer, separator: String, increment: String): Writer {
        value.joinTo(writer, ",$separator$increment", "[$separator$increment", "$separator]") {
            it.toJson(writer, separator + increment, increment)
            ""
        }
        return writer
    }

    operator fun plus(body: KotONBuilder.() -> Any): KotONArray {
        value += kotON(body)
        return this
    }

    override operator fun get(index: Int): KotON<Any> {
        return value[index]
    }
}

data class KotONEntry(val content: Map<String, KotON<Any>> = emptyMap()) : KotON<Any>() {
    override fun toJson(writer: Writer, separator: String, increment: String): Writer {
        return content.entries.joinTo(writer, ",$separator$increment", "{$separator$increment", "$separator}") {
            writer.write("\"${it.key.escape()}\": ")
            it.value.toJson(writer, separator + increment, increment)
            ""
        }
    }

    override operator fun get(index: String, vararg key: String): KotON<Any> {
        return content[index]?.let {
            if (key.isEmpty()) {
                it
            } else {
                it.get(key[0], *key.drop(1).toTypedArray())
            }
        } ?: KotONVal<Any>(null)
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

fun kotON(value: Any): KotONVal<Any> {
    return KotONVal(value)
}

inline fun kotON(init: KotONBuilder.() -> Any): KotON<Any> {
    val root = KotONBuilder()
    root.init()
    return root.build()
}

fun kotON(vararg bodies: KotONBuilder.() -> Unit): KotONArray {
    val kotON = KotONArray()
    bodies.forEach {
        kotON + it
    }
    return kotON
}
