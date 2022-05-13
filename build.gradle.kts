import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    application
}

group = "cn.ryoii"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val miraiVersion = "2.10.0"

dependencies {
    compileOnly("net.mamoe:mirai-core-api:$miraiVersion")

    testImplementation(kotlin("test"))
    testImplementation("net.mamoe:mirai-core-api:$miraiVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}