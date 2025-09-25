pluginManagement {
    repositories {
        // Explicit Google Maven endpoints first (some networks need this)
        maven { url = uri("https://dl.google.com/dl/android/maven2/") }
        maven { url = uri("https://maven.google.com") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    // Allow module-level repos so we can add the explicit URLs there too
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        maven { url = uri("https://dl.google.com/dl/android/maven2/") }
        maven { url = uri("https://maven.google.com") }
        google()
        mavenCentral()
    }
}
rootProject.name = "SnakeIce"
include(":app")
