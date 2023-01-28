import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.7" apply false
    id("io.spring.dependency-management") version "1.0.15.RELEASE" apply false
    kotlin("jvm") version "1.7.22" apply false
    kotlin("kapt") version "1.7.22" apply false
    kotlin("plugin.spring") version "1.7.22" apply false
    kotlin("plugin.jpa") version "1.7.22" apply false
    kotlin("plugin.serialization") version "1.7.22" apply false
    id("io.ktor.plugin") version "2.2.2" apply false
}

allprojects {
    group = "finance.tegro"
    version = "1.5.7"

    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
