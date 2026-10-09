import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}

val authBaseUrl = providers.gradleProperty("AUTH_PUBLIC_BASE_URL")
    .orElse(providers.environmentVariable("AUTH_PUBLIC_BASE_URL"))
    .orElse(localProperties.getProperty("AUTH_PUBLIC_BASE_URL", "http://192.168.1.68:8080"))
    .map(String::trim)
    .orElse("http://192.168.1.68:8080")

val supabasePublishableKey = providers.gradleProperty("SUPABASE_PUBLISHABLE_KEY")
    .orElse(providers.environmentVariable("SUPABASE_PUBLISHABLE_KEY"))
    .orElse(localProperties.getProperty("SUPABASE_PUBLISHABLE_KEY", ""))
    .map(String::trim)
    .orElse("")

android {
    namespace = "dev.alimmz.atlasfly.core.network"
    compileSdk = 37
    defaultConfig {
        minSdk = 24
        buildConfigField(
            "String",
            "AUTH_PUBLIC_BASE_URL",
            "\"${authBaseUrl.get().replace("\\", "\\\\").replace("\"", "\\\"")}\"",
        )
        buildConfigField(
            "String",
            "SUPABASE_PUBLISHABLE_KEY",
            "\"${supabasePublishableKey.get().replace("\\", "\\\\").replace("\"", "\\\"")}\"",
        )
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
dependencies {
    implementation(projects.core.local)

    api(libs.ktor.client.core)
    api(libs.ktor.client.okhttp)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.client.logging)
    api(libs.ktor.serialization.kotlinx.json)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    debugImplementation(libs.chucker.full)
    releaseImplementation(libs.chucker.noop)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
