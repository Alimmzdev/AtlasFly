pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "AtlasFly"
include(":app")
include(":core:design-system")
include(":core:presentation")
include(":core:data")
include(":core:domain")
include(":core:network")
include(":core:local")
include(":service:data")
include(":service:domain")
include(":feature:home")
include(":feature:search")
include(":feature:travel")
include(":feature:flight")
include(":feature:profile")
include(":feature:auth")
include(":core:lib")
include(":core:navigation")
