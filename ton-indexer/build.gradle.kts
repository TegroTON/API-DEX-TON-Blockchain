@file:Suppress("PropertyName")

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin")
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

val ktor_version: String by project
val exposed_version: String by project
val ton_kotlin_version: String by project
val ydb_version: String by project

dependencies {
    implementation("io.ktor:ktor-server-cio:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")

    implementation("org.ton:ton-kotlin-liteclient:$ton_kotlin_version")
    implementation("org.ton:ton-kotlin-contract:$ton_kotlin_version")

    implementation("tech.ydb:ydb-sdk-core:$ydb_version")
    implementation("tech.ydb:ydb-sdk-table:$ydb_version")
    implementation("tech.ydb.auth:yc-auth-provider:$ydb_version")

    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("org.jetbrains.kotlinx:atomicfu:0.23.2")
}

ktor {
    docker {
        jreVersion.set(io.ktor.plugin.features.JreVersion.JRE_17)
        portMappings.set(
            listOf(
                io.ktor.plugin.features.DockerPortMapping(
                    80,
                    8080,
                    io.ktor.plugin.features.DockerPortMappingProtocol.TCP
                )
            )
        )
        providers.gradleProperty("docker.imageName").orNull?.let {
            localImageName.set(it)
        }
        providers.gradleProperty("docker.imageTag").orNull?.let {
            imageTag.set(it)
        }
    }
}

application {
    mainClass.set("finance.tegro.tonindexer.TonIndexerKt")
}
