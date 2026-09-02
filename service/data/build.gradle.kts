plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

android {
    namespace = "dev.alimmz.atlasfly.service.data"
    compileSdk = 37
    defaultConfig {
        minSdk = 24
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
