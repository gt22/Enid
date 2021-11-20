plugins {
    kotlin("jvm") version "1.5.21"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("http://52.48.142.75/maven")
        isAllowInsecureProtocol = true
    }
    maven {
        name = "m2-dv8tion"
        url = uri("https://m2.dv8tion.net/releases")
    }
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.frgm:dawnbreaker:0.1.3.4")
//    implementation("net.dv8tion:JDA:4.3.0_300")
    implementation("com.github.DV8FromTheWorld:JDA:a365d01")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("org.slf4j:slf4j-simple:1.7.30")

    implementation("io.ktor:ktor:1.6.2")
    implementation("io.ktor:ktor-client:1.6.2")
    implementation("io.ktor:ktor-client-okhttp:1.6.2")

    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.3")

    implementation("org.apache.commons:commons-text:1.9")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "Enid"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "15"
}

