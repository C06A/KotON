package com.helpchoice.kotlin.koton

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.StringSpec
import java.lang.NullPointerException

class KotONSpec : StringSpec() {
    init {
        "empty" {
            kotON { }.toJson() shouldBe "{}"
        }

        "numbers" {
            kotON(42).toJson() shouldBe "42"
            kotON(3.14).toJson() shouldBe "3.14"
        }

        "boolean" {
            kotON(true).toJson() shouldBe "true"
            kotON(false).toJson() shouldBe "false"
        }

        "simple collection" {
            kotON {
                "string" to "string value"
                "integer" to 42
                "float" to 3.14
                "boolean true" to true
                "boolean false" to false
            }.toJson() shouldBe """
                {
                    "string": "string value",
                    "integer": 42,"float": 3.14,
                    "boolean true": true,
                    "boolean false": false
                }""".lines().map { it.trim() }.joinToString("")
        }

        "array of simple" {
            kotON {
                "array"[
                        { "stringElement" to "value of an element" },
                        { "intKey" to 42; "floatKey" to 3.14 },
                        {
                            "boolTrue" to true
                            "booleFalse" to false
                        }
                ]
            }.toJson() shouldBe """
                {"array": [
                    {"stringElement": "value of an element"},
                    {"intKey": 42,"floatKey": 3.14},
                    {"boolTrue": true,"booleFalse": false}
                ]}
                """.lines().map { it.trim() }.joinToString("")
        }

        "complex structure" {
            val doc = kotON {
                "string" to "string value"
                "integer" to 42
                "array"[
                        { "stringElement" to "value of an element" },
                        { "intKey" to 42; "floatKey" to 3.14 },
                        {
                            "boolTrue" to true
                            "booleFalse" to false
                        }
                ]
                "float" to 3.14
                "boolean true" to true
                "null value" to null
                "subStruct" {
                    "substring" to "string value"
                    "subinteger" to 42
                    "null subvalue" to null
                    "subfloat" to 3.14
                    "subarray"[
                            { "stringElement" to "value of an element" },
                            { "intKey" to 42; "floatKey" to 3.14 },
                            {
                                "boolTrue" to true
                                "booleFalse" to false
                            }
                    ]
                    "subboolean true" to true
                    "subboolean false" to false
                }
                "boolean false" to false
            }

            doc["string"]() shouldBe "string value"
            doc["integer"]() shouldBe 42
            doc["float"]() shouldBe 3.14
            doc["boolean true"]() shouldBe true
            doc["boolean false"]() shouldBe false
            shouldThrow<NullPointerException> { doc["null value"]() }
                    .apply { message shouldBe "Object contains no value" }
            shouldThrow<NullPointerException> { doc["unexisting"]() }
                    .apply { message shouldBe "Object contains no value" }

            doc("string") shouldBe "string value"
            doc("integer") shouldBe 42
            doc("float") shouldBe 3.14
            doc("boolean true") shouldBe true
            doc("boolean false") shouldBe false
            doc("null value") shouldBe null
            doc("unexisting") shouldBe null

            doc["string"]<String>() shouldBe "string value"
            doc["integer"]<Int>() shouldBe 42
            doc["float"]<Float>() shouldBe 3.14
            doc["boolean true"]<Boolean>() shouldBe true
            doc["boolean false"]<Boolean>() shouldBe false
            shouldThrow<NullPointerException> { doc["null value"]<Int>() }
                    .apply { message shouldBe "Object contains no value" }
            shouldThrow<NullPointerException> { doc["null value"]<String>() }
                    .apply { message shouldBe "Object contains no value" }
            shouldThrow<NullPointerException> { doc["null value"]<Boolean>() }
                    .apply { message shouldBe "Object contains no value" }
            shouldThrow<NullPointerException> { doc["null value"]<Map<String, Any>>() }
                    .apply { message shouldBe "Object contains no value" }

            doc<String>("string") shouldBe "string value"
            doc<Int>("integer") shouldBe 42
            doc<Float>("float") shouldBe 3.14
            doc<Boolean>("boolean true") shouldBe true
            doc<Boolean>("boolean false") shouldBe false
            doc<Int>("null value") shouldBe null
            doc<String>("null value") shouldBe null
            doc<Boolean>("null value") shouldBe null
            doc<Map<String, Any>>("null value") shouldBe null

            doc("string", cls = String::class.java) shouldBe "string value"
            doc("integer", cls = Int::class.java) shouldBe 42
            doc("float", cls = Float::class.java) shouldBe 3.14
            doc("boolean true", cls = Boolean::class.java) shouldBe true
            doc("boolean false", cls = Boolean::class.java) shouldBe false
            doc("null value", cls = Int::class.java) shouldBe null
            doc("null value", cls = String::class.java) shouldBe null
            doc("null value", cls = Boolean::class.java) shouldBe null
            doc("null value", cls = Map::class.java) shouldBe null

            doc["array"][0]["stringElement"]<String>() shouldBe "value of an element"
            doc["array"][1]["intKey"]<Int>() shouldBe 42
            doc["array"][1]["floatKey"]<Float>() shouldBe 3.14
            doc["array"][2]["boolTrue"]<Boolean>() shouldBe true
            doc["array"][2]["booleFalse"]<Boolean>() shouldBe false

            doc["array"][0]<String>("stringElement") shouldBe "value of an element"
            doc["array"][1]<Int>("intKey") shouldBe 42
            doc["array"][1]<Float>("floatKey") shouldBe 3.14
            doc["array"][2]<Boolean>("boolTrue") shouldBe true
            doc["array"][2]<Boolean>("booleFalse") shouldBe false

            val expect = kotON {
                "expected"[
                        { "stringElement" to "value of an element" },
                        { "intKey" to 42; "floatKey" to 3.14 },
                        {
                            "boolTrue" to true
                            "booleFalse" to false
                        }
                ]
            }
            doc["subStruct"]["subarray"] shouldBe expect["expected"]
            doc["subStruct", "subarray"][1]["intKey"]<Int>() shouldBe 42
            doc["subStruct", "subinteger"]<Int>() shouldBe 42
            shouldThrow<NullPointerException> { doc["subStruct", "null subvalue"]() }
                    .apply { message shouldBe "Object contains no value" }

            val expected = kotON(
                    { "stringElement" to "value of an element" },
                    { "intKey" to 42; "floatKey" to 3.14 },
                    {
                        "boolTrue" to true
                        "booleFalse" to false
                    }
            )
            doc["subStruct", "subarray"] shouldBe expected
        }

        "pretty print"
        {
            val doc = kotON {
                "string" to "string value"
                "integer" to 42
                "array"[
                        { "stringElement" to "value of an element" },
                        { "intKey" to 42; "floatKey" to 3.14 },
                        {
                            "boolTrue" to true
                            "booleFalse" to false
                        }
                ]
                "float" to 3.14
                "boolean true" to true
                "subStruct" {
                    "substring" to "string value"
                    "subinteger" to 42
                    "subfloat" to 3.14
                    "subarray"[
                            { "stringElement" to "value of an element" },
                            { "intKey" to 42; "floatKey" to 3.14 },
                            {
                                "boolTrue" to true
                                "booleFalse" to false
                            }
                    ]
                    "subboolean true" to true
                    "subboolean false" to false
                }
                "boolean false" to false
            }

            doc.toJson("|", "-").shouldBe("""
                {
                |-"string": "string value",
                |-"integer": 42,
                |-"array": [
                |--{
                |---"stringElement": "value of an element"
                |--},
                |--{
                |---"intKey": 42,
                |---"floatKey": 3.14
                |--},
                |--{
                |---"boolTrue": true,
                |---"booleFalse": false
                |--}
                |-],
                |-"float": 3.14,
                |-"boolean true": true,
                |-"subStruct": {
                |--"substring": "string value",
                |--"subinteger": 42,
                |--"subfloat": 3.14,
                |--"subarray": [
                |---{
                |----"stringElement": "value of an element"
                |---},
                |---{
                |----"intKey": 42,
                |----"floatKey": 3.14
                |---},
                |---{
                |----"boolTrue": true,
                |----"booleFalse": false
                |---}
                |--],
                |--"subboolean true": true,
                |--"subboolean false": false
                |-},
                |-"boolean false": false
                |}
                """.trimIndent().lines().joinToString(""))

            doc.toJson("\n", "  ").shouldBe("""
                {
                  "string": "string value",
                  "integer": 42,
                  "array": [
                    {
                      "stringElement": "value of an element"
                    },
                    {
                      "intKey": 42,
                      "floatKey": 3.14
                    },
                    {
                      "boolTrue": true,
                      "booleFalse": false
                    }
                  ],
                  "float": 3.14,
                  "boolean true": true,
                  "subStruct": {
                    "substring": "string value",
                    "subinteger": 42,
                    "subfloat": 3.14,
                    "subarray": [
                      {
                        "stringElement": "value of an element"
                      },
                      {
                        "intKey": 42,
                        "floatKey": 3.14
                      },
                      {
                        "boolTrue": true,
                        "booleFalse": false
                      }
                    ],
                    "subboolean true": true,
                    "subboolean false": false
                  },
                  "boolean false": false
                }""".trimIndent())
        }

        "escaping"
        {
            val doc = kotON {
                "back slash" to "back\\slash"
                "double quote" to "double\"quote"
                "carriage return" to "carriage\rreturn"
                "tab char" to "tab\tchar"
                "new line" to "new\nline"
                "form forward" to "form\u000Cforward"
                "back ward" to "back\bward"
            }

            doc.toJson("\n", "  ").shouldBe("""
                {
                  "back slash": "back\\slash",
                  "double quote": "double\"quote",
                  "carriage return": "carriage\rreturn",
                  "tab char": "tab\tchar",
                  "new line": "new\nline",
                  "form forward": "form\fforward",
                  "back ward": "back\bward"
                }
                """.trimIndent())
        }
    }
}
