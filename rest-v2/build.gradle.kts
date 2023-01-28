@file:Suppress("PropertyName")

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin")
    application
}

repositories {
    mavenCentral()
}

val ktor_version = "2.2.2"

dependencies {
    implementation("io.ktor:ktor-server-cio:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
}

application {
    mainClass.set("io.ktor.server.cio.EngineMain")
}

ktor {
    docker {
        jreVersion.set(io.ktor.plugin.features.JreVersion.JRE_17)
        portMappings.set(listOf(
            io.ktor.plugin.features.DockerPortMapping(
                80,
                8080,
                io.ktor.plugin.features.DockerPortMappingProtocol.TCP
            )
        ))
        providers.gradleProperty("docker.imageName").orNull?.let {
            localImageName.set(it)
        }
        providers.gradleProperty("docker.imageTag").orNull?.let {
            imageTag.set(it)
        }
    }
}
