plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

val rootApiBaseUrl = gradle.extra.takeIf { it.has("apiBaseUrl") }?.get("apiBaseUrl") as? String ?: "http://10.0.2.2:8080"

android {
    namespace = "com.wiwchat.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.wiwchat.app"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

}



ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.navigation.compose)
    //  UI бандлы
    implementation(libs.bundles.android.ui)
    implementation(libs.bundles.android.base)
    implementation(libs.bundles.compose)
    // Koin
    implementation(libs.koin.android)
    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.compose)
    ksp(libs.hilt.compiler)

    // zxing
    implementation(libs.zxing.android.embedded)
    // lifecycle
    implementation(libs.bundles.lifecycle)
    ksp(libs.lifecycle.compiler)
    // DataStore
    implementation(libs.data.store)
    // Корутины
    implementation(libs.kotlinx.coroutines.core)
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    // Тесты
    testImplementation(libs.junit)
    //  второй раз платформу Compose BOM подключаем строго для тестовой конфигурации
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.bundles.tests)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    // Дебаг-тесты
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.tooling.preview)

    implementation(project(":shared"))
}
