# KotON
Kotlin DSL to create a JSON document

In order to use this library include in your `build.gradle` file follow

```$groovy
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

This library was inspired by [KTON](https://github.com/Jire/KTON). Unlike later
KotON doesn't allocate both array and map for each instance. Instead it create
instances of different classes to hold different type of data.

Also KotON provides the toJson() method to convert the whole structure into valid
JSON string.
