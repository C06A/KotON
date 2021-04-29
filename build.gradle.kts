import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("org.jetbrains.kotlin.jvm") version "1.3.60"
    id("org.jetbrains.kotlin.kapt") version "1.3.60"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.60"
    id("com.github.johnrengelman.shadow") version "4.0.2"
}

group = "com.helpchoice.kotlin"
version = "1.1.0-SNAPSHOT"


val kotlinVersion: String by project

repositories {
    mavenCentral()
    maven("https://jcenter.bintray.com")
}

dependencyManagement {
    imports {
        mavenBom("io.micronaut:micronaut-bom:1.1.1")
    }
}

//configurations {
//    // for dependencies that are needed for development only
////    developmentOnly {
////
////    }
//}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")

    testCompile("junit:junit:4.12")
    testCompile("io.kotlintest:kotlintest:2.0.0")

    testCompile("org.junit.jupiter:junit-jupiter-api")
    testCompile("org.jetbrains.spek:spek-api:1.1.5")
    testCompile("io.micronaut.test:micronaut-test-junit5")
    testRuntime("org.junit.jupiter:junit-jupiter-engine")
    testRuntime("org.jetbrains.spek:spek-junit-platform-engine:1.1.5")
}

allOpen {
    annotation("io.micronaut.aop.Around")
}

val shadowJar: ShadowJar by tasks
shadowJar.mergeServiceFiles()

tasks.withType(KotlinCompile::class) {
    kotlinOptions {
        jvmTarget = "1.8"
        //Will retain parameter names for Java reflection
        javaParameters = true
    }
}

val run: JavaExec by tasks
run.apply {
    //run.classpath += configurations.developmentOnly
    jvmArgs("-noverify", "-XX:TieredStopAtLevel=1", "-Dcom.sun.management.jmxremote")
}

//// use JUnit 5 platform
val test: Test by tasks
test.apply {
    useJUnitPlatform()
//test.classpath += configurations.developmentOnly
}




//val writeNewPom: WriteNewPom by tasks
//task writeNewPom {
//    group "build"
//
//    doLast {
//        pom {
//            project {
//                inceptionYear "2008"
//                licenses {
//                    license {
//                        name "The Apache Software License, Version 2.0"
//                        url "http://www.apache.org/licenses/LICENSE-2.0.txt"
//                        distribution "repo"
//                    }
//                }
//            }
//        }.writeTo("$libsDir/$project.name-${version}.pom")
//    }
//}
//
//task sourcesJar(type: Jar, dependsOn: classes) {
//    group "build"
//
//    classifier = "sources"
//    from sourceSets.main.allSource
//}
//
//task javadocJar(type: Jar, dependsOn: javadoc) {
//    group "build"
//
//    classifier = "javadoc"
//    from javadoc.destinationDir
//}
//
//artifacts {
//    archives sourcesJar
//            archives javadocJar
//}
