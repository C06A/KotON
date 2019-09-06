package com.helpchoice.kotlin.koton

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.StringSpec

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

            doc["string"]() shouldBe "string value"
            doc["integer"]() shouldBe 42
            doc["float"]() shouldBe 3.14
            doc["boolean true"]() shouldBe true
            doc["boolean false"]() shouldBe false
            shouldThrow<IllegalAccessException> {
                doc["unexisting"]
            }.apply {
                message shouldBe "Key access is not supported by this instance"
            }

            doc["string"]<String>() shouldBe "string value"
            doc["integer"]<Int>() shouldBe 42
            doc["float"]<Float>() shouldBe 3.14
            doc["boolean true"]<Boolean>() shouldBe true
            doc["boolean false"]<Boolean>() shouldBe false

            doc["array"][0]["stringElement"]<String>() shouldBe "value of an element"
            doc["array"][1]["intKey"]<Int>() shouldBe 42
            doc["array"][1]["floatKey"]<Float>() shouldBe 3.14
            doc["array"][2]["boolTrue"]<Boolean>() shouldBe true
            doc["array"][2]["booleFalse"]<Boolean>() shouldBe false

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

        "pretty print" {
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

        "escaping" {
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
