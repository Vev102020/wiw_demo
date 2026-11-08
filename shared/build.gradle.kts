import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.build.konfig)
    alias(libs.plugins.ksp)
}

val isRelease = project.hasProperty("release")

configure<com.codingfeline.buildkonfig.gradle.BuildKonfigExtension> {
    packageName = "com.wiwchat.shared.config"

    defaultConfigs {
        // Мы запекаем НЕ домен, а режим работы приложения!
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN,
            "IS_RELEASE",
            isRelease.toString()
        )
    }
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    // Настройка серверного (JVM) таргета
    jvm("server") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    androidLibrary {
        namespace = "com.wiwchat.shared"
        //noinspection GradleDependency
        compileSdk = 35

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    // Глобальные аргументы для всех компиляторов модуля :shared
    compilerOptions {
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }

    sourceSets {
        named("commonMain") {
            dependencies {
                api(libs.bundles.ktor.client)
                api(libs.kotlinx.coroutines.core)
                implementation(libs.inject.api)
                implementation(libs.koin.core)
                // Room
                // пишем api, чтобы модуль :app видел типы данных Room (Dao, Database)
                api(libs.room.runtime)
                implementation(libs.sqlite.bundled)
            }
        }

        named("serverMain") {
            // Зависимости специфичные для серверного jvm-слоя shared модуля (если понадобятся)
        }
        named("androidMain") {
            // Зависимости специфичные для android-слоя shared модуля
        }
    }
}

dependencies {
    // Направляем компилятор Room строго в Android-часть shared-модуля
    add("kspAndroid", libs.room.compiler)

    // Направляем компилятор Room строго в Серверную (JVM) часть shared-модуля
    add("kspServer", libs.room.compiler)
}