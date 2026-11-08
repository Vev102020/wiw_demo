// игнорируем предупреждение для строки repositoriesMode
@file:Suppress("UnstableApiUsage")

rootProject.name = "wiwchat"

include(":shared", ":server", ":app")


// Получаем URL из local.properties
val localProperties = java.util.Properties()
val localPropsFile = java.io.File(rootDir, "local.properties")

if (localPropsFile.exists()) {
    localPropsFile.reader().use { reader ->
        localProperties.load(reader)
    }
}
val apiBaseUrl = localProperties.getProperty("API_BASE_URL", "http://10.0.2.2:8080")
gradle.extra["apiBaseUrl"] = apiBaseUrl

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    // через libs не будет работать
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}



