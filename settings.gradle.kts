pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositories {
        maven("https://raw.githubusercontent.com/HereLiesAz/AzNavRail/update-maven/") {
            content { includeModule("com.github.HereLiesAz.AzNavRail", "aznavrail-updates") }
        }
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}
rootProject.name = "WiFi Haqueur"
include(":app")
