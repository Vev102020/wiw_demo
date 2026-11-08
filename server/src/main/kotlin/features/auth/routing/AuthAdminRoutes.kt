package features.auth.routing

import com.wiwchat.shared.core.user.data.dto.enums.UserRole
import core.errors.*
import features.auth.domain.service.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import java.util.UUID
import kotlin.text.uppercase

class AuthorizationConfig {
    val allowedRoles = mutableSetOf<String>()

    fun hasAnyRole(vararg roles: UserRole) {
        allowedRoles.addAll(roles.map { it.name })
    }
}

val RoleAuthorizationPlugin = createRouteScopedPlugin(
    name = "RoleAuthorizationPlugin",
    createConfiguration = ::AuthorizationConfig
) {
    // Подписываемся на хук окончания проверки аутентификации JWT
    on(AuthenticationChecked) { call ->
        // Извлекаем principal (он тут гарантированно инициализирован Ktor-ом)
        val principal = call.principal<JWTPrincipal>()
        val roleFromToken = principal?.payload?.getClaim("role")?.asString()

        // Проверяем, входит ли роль пользователя в список разрешенных для этого роута
        if (roleFromToken == null || !pluginConfig.allowedRoles.contains(roleFromToken)) {
            throw ForbiddenException()
        }
    }
}


fun Route.authAdminRoutes(
    authService: AuthService
) {
    val authRateLimit = RateLimitName("auth")

    rateLimit(authRateLimit) {
        route("/admin/users/{id}") {

            // Снятия ограничений бана
            post("/activate") {
                // Разрешение на действия
                install(RoleAuthorizationPlugin) {
                    hasAnyRole(UserRole.IMPERATOR, UserRole.INQUISITOR, UserRole.SUPPORT)
                }

                val targetId = call.getValidIdParameter()
                val response = authService.activateUser(targetId)
                call.respond(HttpStatusCode.OK, response)
            }

            // Блокировки пользователя
            post("/suspend") {
                // Разрешение на действия
                install(RoleAuthorizationPlugin) {
                    hasAnyRole(UserRole.IMPERATOR, UserRole.INQUISITOR)
                }

                val targetId = call.getValidIdParameter()
                val response = authService.suspendUser(targetId)
                call.respond(HttpStatusCode.OK, response)
            }

            // Изменения глобальной роли пользователя
            put("/role") {
                // Разрешение на действия
                install(RoleAuthorizationPlugin) {
                    hasAnyRole(UserRole.IMPERATOR)
                }

                val targetId = call.getValidIdParameter()

                val roleParam = call.request.queryParameters["type"]?.uppercase()
                    ?: throw InvalidRequestFormatException()

                val newRole = try {
                    UserRole.valueOf(roleParam)
                } catch (e: IllegalArgumentException) {
                    throw InvalidUserRoleException()
                }

                val response = authService.updateUserRole(targetId, newRole)
                call.respond(HttpStatusCode.OK, response)
            }

        }
    }
}

private fun ApplicationCall.getValidIdParameter(name: String = "id"): UUID {
    val param = parameters[name] ?: throw InvalidIdentifierException()
    return try {
        UUID.fromString(param)
    } catch (e: IllegalArgumentException) {
        throw InvalidIdentifierException()
    }
}