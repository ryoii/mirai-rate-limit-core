plugins {
    kotlin("jvm") version "1.6.21"
    id("me.him188.maven-central-publish") version "1.0.0-dev-3"
}

group = "cn.ryoii"
version = "1.1.0"

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

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

mavenCentralPublish {
    useCentralS01()

    singleDevGithubProject("ryoii", projectName)
    licenseFromGitHubProject("AGPL-3.0", "master")
}