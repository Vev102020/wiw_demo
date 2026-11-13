package core.plugins

import features.auth.domain.service.AuthService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import features.auth.routing.authUserRoutes
import features.auth.routing.authAdminRoutes
import features.profile.routing.profileRoutes
import org.koin.ktor.ext.inject

fun Application.configureRouting(

) {
    val authService: AuthService by inject()

    routing {
        get("/") {
            call.respondText(
                text = "Сервер успешно запущен!",
                contentType = io.ktor.http.ContentType.Text.Plain
            )
        }
        route("/api/v1") {
            // Пользователи
            authUserRoutes(authService)
            // Админы
            authAdminRoutes(authService)
            // Профиль
            profileRoutes()
        }
    }
}