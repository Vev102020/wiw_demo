package features.profile.routing

import com.wiwchat.shared.common.domain.errors.ProfileErrorCode
import com.wiwchat.shared.features.profile.data.dto.*
import com.wiwchat.shared.features.profile.domain.validator.ProfileValidator
import core.errors.*
import features.profile.domain.service.UserProfileService
import io.ktor.server.routing.Route
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.routing.*
import java.util.UUID
import org.koin.ktor.ext.inject
import io.ktor.server.request.receive
import io.ktor.server.response.respond

fun Route.profileRoutes() {
    val profileService by inject<UserProfileService>()
    val profileRateLimit = RateLimitName("profile")

    authenticate("auth-jwt") {
        rateLimit(profileRateLimit) {
            route("/users/me") {

                /**
                 * Обновление отображаемого имени
                 */
                patch("/display-name") {
                    val principal = call.principal<JWTPrincipal>()
                    val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                        ?: throw InvalidTokenException()
                    val userIdUUID = UUID.fromString(userIdStr)

                    val request = call.receive<UpdateDisplayNameRequest>()

                    val nameError = ProfileValidator.validateDisplayName(request.displayName)
                    if (nameError != null) {
                        throw when (nameError) {
                            ProfileErrorCode.PROFILE_EMPTY_DISPLAY_NAME -> EmptyDisplayNameException()
                            ProfileErrorCode.PROFILE_DISPLAY_NAME_INVALID_LENGTH -> InvalidDisplayNameLengthException()
                            else -> InvalidRequestFormatException()
                        }
                    }

                    val updatedProfile =
                        profileService.updateDisplayName(userIdUUID, request.displayName)
                    call.respond(HttpStatusCode.OK, updatedProfile)
                }

                /**
                 * Обновление уникального username
                 */
                patch("/username") {
                    val principal = call.principal<JWTPrincipal>()
                    val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                        ?: throw InvalidTokenException()
                    val userIdUUID = UUID.fromString(userIdStr)

                    val request = call.receive<UpdateUsernameRequest>()

                    val usernameError = ProfileValidator.validateUsername(request.username)
                    if (usernameError != null) {
                        throw when (usernameError) {
                            ProfileErrorCode.PROFILE_EMPTY_USERNAME -> EmptyUsernameException()
                            ProfileErrorCode.PROFILE_USERNAME_INVALID_LENGTH -> InvalidUsernameLengthException()
                            ProfileErrorCode.PROFILE_USERNAME_INVALID_FORMAT -> InvalidUsernameFormatException()
                            else -> InvalidRequestFormatException()
                        }
                    }

                    val updatedProfile =
                        profileService.updateUsername(userIdUUID, request.username)
                    call.respond(HttpStatusCode.OK, updatedProfile)
                }
            }
        }
    }
}