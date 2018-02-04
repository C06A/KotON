package com.helpchoice.kotlin.koton

import java.io.*

/**
 * This was inspired by project https://github.com/Jire/KTON.
 *
 * Here I spread content into class hierarchy which helps to scope functions
 * and doesn't require both Map and Array for each instance.
 * As a result access to the value requires extra parenthesis.
 *
 * Also I added toJson() function to convert the Object into (you guessed it) JSON.
 */
open class KotON() {
    open fun toJson(writer: Writer): Writer {
        writer.write("{}")
        return writer
    }

    fun toJson(): String {
        return toJson(StringWriter()).toString()
    }

    open operator fun get(key: String): KotON {
        if (this is KotONEntry) {
            return this[key]
        }
        throw Exception("Key access is not supported by this instance")
    }

    open operator fun get(index: Int): KotON {
        if (this is KotONArray) {
            return this[index]
        }
        throw Exception("Index is not supported by this instance")
    }

    open operator fun invoke(): Any? {
        return null
    }
}

data class KotONVal<out T>(val value: T): KotON() {
    override fun toJson(writer: Writer): Writer {
        writer.write(
                when (value) {
                    is String -> "\"$value\""
                    else -> value.toString()
                })
        return writer
    }

    override operator fun invoke(): Any? {
        return value
    }
}

data class KotONArray(val value: ArrayList<KotON> = ArrayList()): KotON() {
    override fun toJson(writer: Writer): Writer {
        value.joinTo(writer, prefix = "[", postfix = "]") {
            it.toJson(writer)
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
    override fun toJson(writer: Writer): Writer {
        return content.entries.joinTo(writer, prefix = "{", postfix = "}") {
            writer.write("\"${it.key}\": ")
            it.value.toJson(writer)
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

inline fun <T> kotON(value: T): KotONVal<T> {
    return KotONVal(value)
}

inline fun kotON(init: KotONBuilder.() -> Any): KotONEntry {
    val root = KotONBuilder()
    val value = root.init()
    return root.build()
}

inline fun kotON(vararg bodies: KotONBuilder.() -> Unit): KotONArray {
    val kotON = KotONArray()
    bodies.forEach {
        kotON + it
    }
    return kotON
}
