plugins {
    id("com.android.library")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "tech.nullexdev.atlasfly.service.data"
    compileSdk = 37
    defaultConfig {
        minSdk = 24
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation(libs.firebase.bom)
    implementation(libs.firebase.auth)
    implementation(projects.core.local)
    implementation(projects.service.domain)

    implementation(libs.datastore)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}