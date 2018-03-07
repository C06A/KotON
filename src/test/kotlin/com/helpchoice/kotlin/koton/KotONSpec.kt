package com.helpchoice.kotlin.koton

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class KotONSpec: StringSpec() {
    init {
        "empty" {
            KotON().toJson() shouldBe "{}"
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
            }.toJson() shouldBe
                    "{\"string\": \"string value\",\"integer\": 42,\"float\": 3.14," +
                    "\"boolean true\": true,\"boolean false\": false}"
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
            }.toJson() shouldBe
                    "{\"array\": [{\"stringElement\": \"value of an element\"}," +
                    "{\"intKey\": 42,\"floatKey\": 3.14}," +
                    "{\"boolTrue\": true,\"booleFalse\": false}]}"
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

            doc["array"][0]["stringElement"]() shouldBe "value of an element"
            doc["array"][1]["intKey"]() shouldBe 42
            doc["array"][1]["floatKey"]() shouldBe 3.14
            doc["array"][2]["boolTrue"]() shouldBe true
            doc["array"][2]["booleFalse"]() shouldBe false

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

            val expected = kotON(
                    { "stringElement" to "value of an element" },
                    { "intKey" to 42; "floatKey" to 3.14 },
                    {
                        "boolTrue" to true
                        "booleFalse" to false
                    }
            )
            doc["subStruct"]["subarray"] shouldBe expected
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

            doc.toJson("|", "-").shouldBe(
                    "{|-\"string\": \"string value\",|-\"integer\": 42,|-\"array\": [|--" +
                            "{|---\"stringElement\": \"value of an element\"|--},|--{|---\"intKey\": 42,|---\"floatKey\": 3.14|--}," +
                            "|--{|---\"boolTrue\": true,|---\"booleFalse\": false|--}|-],|-\"float\": 3.14,|-\"boolean true\": true," +
                            "|-\"subStruct\": {|--\"substring\": \"string value\",|--\"subinteger\": 42,|--\"subfloat\": 3.14," +
                            "|--\"subarray\": [|---{|----\"stringElement\": \"value of an element\"|---},|---" +
                            "{|----\"intKey\": 42,|----\"floatKey\": 3.14|---},|---{|----\"boolTrue\": true,|----\"booleFalse\": false|---}|--]," +
                            "|--\"subboolean true\": true,|--\"subboolean false\": false|-},|-\"boolean false\": false|}")

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
                "new line" to "new\nline"
                "curredg return"
            }
        }
    }
}
