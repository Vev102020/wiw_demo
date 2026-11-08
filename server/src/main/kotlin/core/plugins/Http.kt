package core.plugins

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureHttp() {
    val domainsString = environment.config.property("cors.allowedDomains").getString()
    val allowedDomains = domainsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        // Разрешаем стандартные заголовки авторизации и типа данных JSON
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        // @TODO: По возможности избегайте этого в производственной среде. Старайтесь свести это к минимуму.
        // @TODO: Заменить anyHost()  на allowHost("wiwchat.com")).
//        anyHost()
        allowedDomains.forEach { domain ->
            allowHost(domain, schemes = listOf("http", "https"))
        }
        // Ограничиваем доступ только вашим официальным доменом (для ИБ)
//        allowHost("wiwchat.com", schemes = listOf("http", "https"))

        // Разрешаем передачу авторизационных токенов/кук (для веб)
        allowCredentials = true
    }
}
