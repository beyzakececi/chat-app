pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // KSP plugin’ini burada, version ile birlikte “apply false” olarak tanımlıyoruz.
        id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ChatApp"
include(":app")
