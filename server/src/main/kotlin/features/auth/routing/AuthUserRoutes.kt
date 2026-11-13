package features.auth.routing

import com.wiwchat.shared.common.domain.errors.AuthErrorCode
import features.auth.domain.service.AuthService
import io.ktor.http.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*
import com.wiwchat.shared.features.auth.data.dto.*
import com.wiwchat.shared.features.auth.domain.constants.AuthConstants
import com.wiwchat.shared.features.auth.domain.validator.AuthValidator
import core.errors.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

fun Route.authUserRoutes(
    authService: AuthService
) {
    val authRateLimit = RateLimitName("auth")

    rateLimit(authRateLimit) {
        route("/auth") {
            /**
             * Регистрация
             */
            post("/register") {
                val request = call.receive<RegisterRequest>()
                val cleanEmail = request.email.trim()

                // Проверка на пустые поля
                if (cleanEmail.isBlank()) throw EmptyEmailException()
                if (request.password.isEmpty()) throw EmptyPasswordException()

                if (cleanEmail.length > AuthConstants.EMAIL_MAX_LENGTH) {
                    throw InvalidEmailException()
                }

                // Валидация почты и пароля
                val emailError = AuthValidator.validateEmail(cleanEmail)
                if (emailError != null) throw InvalidEmailException()

                val passwordError = AuthValidator.validatePassword(request.password)
                if (passwordError != null) {
                    throw when (passwordError) {
                        AuthErrorCode.AUTH_EMPTY_PASSWORD -> EmptyPasswordException()
                        AuthErrorCode.AUTH_PASSWORD_INVALID_LENGTH -> LengthPasswordException()
                        AuthErrorCode.AUTH_PASSWORD_NO_DIGIT -> DigitPasswordException()
                        AuthErrorCode.AUTH_PASSWORD_NO_UPPERCASE -> UppercasePasswordException()
                        AuthErrorCode.AUTH_PASSWORD_NO_LOWERCASE -> LowercasePasswordException()
                        else -> InvalidCredentialsException()
                    }
                }

                // Вызов сервиса
                val response = authService.register(request)
                call.respond(HttpStatusCode.OK, response)
            }

            /**
             * Верификация
             */
            post("/verify-otp") {
                val request = call.receive<VerifyOtpRequest>()

                // Проверка на пустые поля
                if (request.email.isBlank()) throw EmptyEmailException()
                if (request.registrationSessionId.isBlank()) throw EmptyVerificationSessionException()
                if (request.code.isBlank()) throw EmptyOtpCodeException()

                // Вызов сервиса и создание записи в БД
                val rawUserAgent = call.request.headers[HttpHeaders.UserAgent]
                val userAgent = parseUserAgentSafely(rawUserAgent)
                val response = authService.verifyOtp(request, userAgent)
                call.respond(HttpStatusCode.Created, response)
            }

            /**
             * Повторная отправка кода верификации
             */
            post("/resend-otp") {
                val request = call.receive<ResendOtpRequest>()

                if (request.email.isBlank()) throw EmptyEmailException()
                if (request.registrationSessionId.isBlank()) throw EmptyVerificationSessionException()

                // Вызываем метод сервиса (реализацию см. ниже)
                val newResponse = authService.resendRegistrationOtp(request)
                call.respond(HttpStatusCode.OK, newResponse)
            }

            /**
             * Авторизация Логин
             */
            post("/login") {
                val request = call.receive<LoginRequest>()
                val cleanEmail = request.email.trim()

                // Проверка на пустые поля
                if (cleanEmail.isBlank()) throw EmptyEmailException()
                if (request.password.isEmpty()) throw EmptyPasswordException()

                // Валидация формата почты
                val emailError = AuthValidator.validateEmail(cleanEmail)
                if (emailError != null) throw InvalidEmailException()


                // Передаем запрос и userAgent в сервис. Сервис сам всё проверит и запишет сессию!
                val rawUserAgent = call.request.headers[HttpHeaders.UserAgent]
                val userAgent = parseUserAgentSafely(rawUserAgent)
                val response = authService.login(request, userAgent)

                call.respond(HttpStatusCode.OK, response)
            }

            /**
             * Забыли пароль. Запрос восстановления через почту
             */
            post("/password/forgot") {
                val request = call.receive<ForgotPasswordRequest>()
                val cleanEmail = request.email.trim()

                if (cleanEmail.isBlank()) throw EmptyEmailException()

                val emailError = AuthValidator.validateEmail(cleanEmail)
                if (emailError != null) throw InvalidEmailException()

                val response = authService.forgotPassword(request)
                call.respond(HttpStatusCode.OK, response)
            }

            /**
             * OTP подтверждение
             */
            post("/password/verify-otp") {
                val request = call.receive<VerifyResetOtpRequest>()

                if (request.email.isBlank()) throw EmptyEmailException()
                if (request.code.isBlank()) throw EmptyOtpCodeException()
                if (request.resetToken.isBlank()) throw EmptyResetTokenException()

                // Проверяем код и возвращаем подтвержденный resetToken
                val response = authService.verifyResetOtp(request)
                call.respond(HttpStatusCode.OK, response)
            }

            /**
             * Сброс пароля с кодом из почты
             */
            post("/password/reset") {
                val request = call.receive<ResetPasswordRequest>()

                // Проверка пустых полей
                if (request.email.isBlank()) throw EmptyEmailException()
                if (request.resetToken.isBlank()) throw EmptyResetTokenException()
                if (request.newPassword.isEmpty()) throw EmptyPasswordException()

                // Валидация нового пароля по стандартам безопасности вашего проекта
                val passwordError = AuthValidator.validatePassword(request.newPassword)
                if (passwordError != null) {
                    throw when (passwordError) {
                        AuthErrorCode.AUTH_EMPTY_PASSWORD -> EmptyPasswordException()
                        AuthErrorCode.AUTH_PASSWORD_INVALID_LENGTH -> LengthPasswordException()
                        AuthErrorCode.AUTH_PASSWORD_NO_DIGIT -> DigitPasswordException()
                        AuthErrorCode.AUTH_PASSWORD_NO_UPPERCASE -> UppercasePasswordException()
                        AuthErrorCode.AUTH_PASSWORD_NO_LOWERCASE -> LowercasePasswordException()
                        else -> InvalidCredentialsException()
                    }
                }
                // Вызываем сервис, который сбросит пароль и удалит все сессии
                authService.resetPassword(request)
                // Возвращаем простой статус успеха
                call.respond(HttpStatusCode.OK, StatusResponse())
            }

            /**
             * Авторизированный пользователь через настройки. Смена пароля
             */
            post("/password/change") {
                val request = call.receive<ChangePasswordRequest?>()
                    ?: throw InvalidRequestFormatException()

                if (request.oldPassword.isBlank() || request.newPassword.isBlank()) {
                    throw InvalidRequestFormatException()
                }

                // Извлекаем userId текущего авторизованного пользователя из JWT токена
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                    ?: throw InvalidTokenException()
                val userIdUUID = UUID.fromString(userIdStr)

                // Извлекаем старый accessToken, по которому сейчас открыт WebSocket текущего устройства
                val currentAccessToken = call.request.headers[HttpHeaders.Authorization]
                    ?.removePrefix("Bearer ")
                    ?.trim()

                // Обрабатываем User-Agent
                val rawUserAgent = call.request.headers[HttpHeaders.UserAgent]
                val userAgent = parseUserAgentSafely(rawUserAgent)

                // Вызываем сервис бизнес-логики
                val response = authService.changePassword(userIdUUID, request, userAgent, currentAccessToken)

                call.respond(HttpStatusCode.OK, response)
            }

            /**
             * Обновление токена
             */
            post("/refresh") {
                val request = call.receive<RefreshRequest?>()
                    ?: throw InvalidRequestFormatException()

                // Извлекаем заголовок устройства (User-Agent)
                val rawUserAgent = call.request.headers[HttpHeaders.UserAgent]
                val userAgent = parseUserAgentSafely(rawUserAgent)

                val loginResponse = authService.refresh(
                    oldRefreshToken = request.refreshToken,
                    userAgent = userAgent
                )

                // Возвращаем клиенту новую пару токенов и свежий профиль
                call.respond(HttpStatusCode.OK, loginResponse)
            }

            /**
             * Выход из аккаунта
             */
            post("/logout") {
                val request = call.receive<RefreshRequest?>()
                    ?: throw InvalidRequestFormatException()

                if (request.refreshToken.isBlank()) {
                    throw InvalidRequestFormatException()
                }

                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                    ?: throw InvalidTokenException()
                val userIdUUID = UUID.fromString(userIdStr)

                // Вызываем метод очистки сессий в сервисе
                val response = authService.logout(userIdUUID, request.refreshToken)

                call.respond(HttpStatusCode.OK, response)
            }

        }

    }

}

