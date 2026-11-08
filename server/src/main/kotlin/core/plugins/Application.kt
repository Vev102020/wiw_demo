package core.plugins

import io.ktor.server.application.Application

fun Application.rootModule() {
    configureStatusPages()
    configureKoin()
    configureSerialization()
    configureHttp()
    configurePostgres()
    configureRateLimiting()
    configureSecurity()
    configureWebsockets()
    configureRouting()
}
