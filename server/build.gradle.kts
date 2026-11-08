
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.application)
    alias(libs.plugins.shadow)

}

group = "com.wiwchat"
version = "1.0.0-SNAPSHOT"

application {
    mainClass.set("io.ktor.server.cio.EngineMain")
}
tasks.withType<JavaExec> {
    systemProperty("io.ktor.development", "true")
}


kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.software.amazon)

    // koin
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    // BD
    implementation(libs.logback.classic)
    implementation(libs.postgresql)
    implementation(libs.hicaricp)
    implementation(libs.h2database.h2)

    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.exposed)
    implementation(libs.bundles.flyway)
    // email
    implementation(libs.javax.mail)
    // bcrypt
    implementation(libs.jbcrypt)
    // parser
    implementation(libs.ua.parser)

    // Test
    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)
}

tasks.shadowJar {
    archiveFileName.set("wiw-chat-server.jar")

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    // Тот самый метод, который собирает плагины Flyway воедино и чинит валидацию
    mergeServiceFiles()
}

tasks.named("startShadowScripts") {
    enabled = false
}
tasks.named("shadowDistTar") {
    enabled = false
}
tasks.named("shadowDistZip") {
    enabled = false
}