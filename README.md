# KotON
"**Kot**lin **O**bject **N**otation" (Pronounced like "***cotton***") is Kotlin DSL to create a JSON document


[![license](https://img.shields.io/github/license/C06A/KotON.svg)](https://github.com/C06A/KotON/blob/master/LICENSE)
[![Download Latest](https://img.shields.io/badge/download-1.0--SNAPSHOT-yellow.svg)](https://raw.githubusercontent.com/C06A/KotON/artifactiry/com/helpchoice/kotlin/koton/1.0-SNAPSHOT/koton-1.0-SNAPSHOT.jar)

In order to use this library include in your `build.gradle` file follow

### Gradle
```groovy
repositories {
    mavenCentral()
    maven {
        url 'https://raw.githubusercontent.com/C06A/KotON/artifactiry'
    }
}

dependencies {
    compile 'com.helpchoice.kotlin:koton:1.0-SNAPSHOT'
}

```

### Maven
```xml
<dependency>
    <groupId>com.helpchoice.kotlin</groupId>
    <artifactId>koton</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```


This library was inspired by [KTON](https://github.com/Jire/KTON). Unlike later
KotON doesn't allocate both array and map for each instance. Instead it create
instances of different classes to hold different type of data.

Also KotON provides the toJson() method to convert the whole structure into valid
JSON string.

KotON object with parentheses returns the internal value. Depend on the instance that value
may be simple Kotlin instance, array or map (see examples below).

To create the root object call the `kotON(...)` function. Depend on provided parameters
it will return instance of the different type.

For examples of different ways to define KotON instances check the kotlintest unit test
in the project. Thous tests create instances of KotON and then validate them by comparing
JSON output to expected String value. 

### Simple values

Function `kotON(...)` with simple parameter like `Int`, `Long`, `Boolean`, etc. will create
an instance which will be represented in JSON as the value of that parameter.

Same function taking `String` parameter get represented in JSON as double-quoted string.

For example:

```Kotlin
kotON(42)() == 42
kotON(42).toJson() == "42"

kotON(3.14)() == 3.14
kotON(3.14).toJson() == "3.14"

kotON(true)() == true
kotON(true).toJson() == "true"

kotON("any text")() == "any text"
kotON(true).toJson() == "\"any text\""
```
`

### Array value

In order to create the array object call the function `kotON(...)` with elements describing lambdas
separated with commas.

```Kotlin
kotON(
    { "start" to 10; "stop" to 100; "step" to 10 },
    {
      "pattern" to "[a-zA-Z0-9]+"
      "skip" to false
    },
    { "validate" to false }
)
```

Function `toJson()` on such instance will return follow String:

```$xslt
[{"start": 10, "stop": 100, "step": 10}, {"pattern": "[a-zA-Z0-9]+", "skip" to false}, {"validate": false}]
```

### Compound value

Overloaded function taking single lambda creates the instance defined in that lambda.

Each element inside lambda could be one of:

1. a map from String to simple value (using `to` keyword)
1. a lambda defining compound value with String prefix
1. an Array value prefixed by String

all these elements can appear in any order. For example:

```Kotlin
kotON {
  "number" to 25
  "text" to "The text content"
  "subElement" {
    "subNumber" to 625
    "subBool" to false
  }
  "subArray"[
    { "start" to 10; "stop" to 100; "step" to 10 },
    {
      "pattern" to "[a-zA-Z0-9]+"
      "skip" to false
    },
    { "validate" to false }
  ]
}
```

For other examples see Unit Tests.
