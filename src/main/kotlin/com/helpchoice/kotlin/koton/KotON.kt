package com.helpchoice.kotlin.koton

import java.io.*

/**
 * This was inspired by project https://github.com/Jire/KTON.
 *
 * Here the content get spread into class hierarchy which helps to scope functions
 * and doesn't require both Map and Array for each instance.
 * As a result access to the value requires extra parenthesis.
 *
 * Also I added toJson() function to convert the Object into (you guessed it) JSON.
 */
open class KotON() {
    open fun toJson(writer: Writer, separator: String = "", increment: String = ""): Writer {
        writer.write("$separator{}")
        return writer
    }

    fun toJson(separator: String = "", increment: String = ""): String {
        return toJson(StringWriter(), separator, increment).toString()
    }

    open operator fun get(key: String): KotON {
        if (this is KotONEntry) {
            return this[key]
        }
        throw IllegalAccessException("Key access is not supported by this instance")
    }

    open operator fun get(index: Int): KotON {
        if (this is KotONArray) {
            return this[index]
        }
        throw IllegalAccessException("Index is not supported by this instance")
    }

    open operator fun invoke(): Any? {
        return null
    }
}

data class KotONVal<out T>(val value: T): KotON() {
    override fun toJson(writer: Writer, separator: String, increment: String): Writer {
        writer.write(
                when (value) {
                    is String -> "\"${value.escape()}\""
                    else -> value.toString()
                })
        return writer
    }

    override operator fun invoke(): Any? {
        return value
    }
}

data class KotONArray(val value: ArrayList<KotON> = ArrayList()): KotON() {
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

    override operator fun get(index: Int): KotON {
        return value[index]
    }
}

data class KotONEntry(val content: Map<String, KotON> = emptyMap()): KotON() {
    override fun toJson(writer: Writer, separator: String, increment: String): Writer {
        return content.entries.joinTo(writer, ",$separator$increment", "{$separator$increment", "$separator}") {
            writer.write("\"${it.key.escape()}\": ")
            it.value.toJson(writer, separator + increment, increment)
            ""
        }
    }

    override operator fun get(key: String): KotON {
        return content[key] ?: super.get(key)
    }
}

data class KotONBuilder(val content: MutableMap<String, KotON> = mutableMapOf()) {
    infix fun <T> String.to(value: T) {
        content[this] = KotONVal(value)
    }

    operator fun String.invoke(body: KotONBuilder.() -> Any) {
        content[this] = kotON(body)
    }

    operator fun String.get(vararg bodies: KotONBuilder.() -> Unit) {
        content[this] = kotON(*bodies)
    }

    fun build(): KotONEntry {
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

fun <T> kotON(value: T): KotONVal<T> {
    return KotONVal(value)
}

inline fun kotON(init: KotONBuilder.() -> Any): KotONEntry {
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
