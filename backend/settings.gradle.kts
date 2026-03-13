pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        kotlin("jvm") version "1.9.24"
        kotlin("plugin.spring") version "1.9.24"
        kotlin("plugin.jpa") version "1.9.24"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "adaptive-testing-backend"
