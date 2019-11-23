buildscript {
    ext.kotlin_version = "1.2.60"

    repositories {
        jcenter()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:${dokka_version}")
    }
}

group = "com.helpchoice.kotlin"
version = "1.1.7"

apply plugin: "java"
apply plugin: "kotlin"
apply plugin: "maven"
apply plugin: 'org.jetbrains.dokka'

sourceCompatibility = 1.8

repositories {
    jcenter()
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")

    testCompile("junit:junit:4.12")

    testCompile("io.kotlintest:kotlintest:2.0.0")

}

compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

task writeNewPom {
    group "build"

    doLast {
        pom {
            project {
                inceptionYear "2008"
                licenses {
                    license {
                        name "The Apache Software License, Version 2.0"
                        url "http://www.apache.org/licenses/LICENSE-2.0.txt"
                        distribution "repo"
                    }
                }
            }
        }.writeTo("$libsDir/$project.name-${version}.pom")
    }
}

task sourcesJar(type: Jar, dependsOn: classes) {
    group "build"

    classifier = "sources"
    from sourceSets.main.allSource
}

dokka {
    outputFormat = 'html'
    outputDirectory = "$buildDir/dokka"
}

task dokkadocJar(type: Jar, dependsOn: dokka) {
    group "build"

    classifier = "javadoc"
    from dokka.outputDirectory
}

artifacts {
    archives sourcesJar
    archives dokkadocJar
}
