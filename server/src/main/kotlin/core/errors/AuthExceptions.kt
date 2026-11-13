package core.errors

import com.wiwchat.shared.common.domain.errors.AuthErrorCode
import io.ktor.http.HttpStatusCode

sealed class AuthException(
    code: AuthErrorCode,
    message: String,
    httpStatus: HttpStatusCode
) : AppException(code, message, httpStatus)

/**
 * Права
 */

class AccountSuspendedException : AuthException(
    code = AuthErrorCode.AUTH_ACCOUNT_SUSPENDED,
    message = "Ваш аккаунт заблокирован.",
    httpStatus = HttpStatusCode.Forbidden
)

class ForbiddenException() : AuthException(
    code = AuthErrorCode.AUTH_INSUFFICIENT_PERMISSIONS,
    message = "У вас недостаточно прав для выполнения этого действия.",
    httpStatus = HttpStatusCode.Forbidden
)
class InvalidUserRoleException() : AuthException(
    code = AuthErrorCode.AUTH_INVALID_USER_ROLE,
    message = "Указана несуществующая или неподдерживаемая роль пользователя.",
    httpStatus = HttpStatusCode.BadRequest
)

/**
 * Поля
 */

class TooManyOtpAttemptsException() : AuthException(
    code = AuthErrorCode.AUTH_TOO_MANY_ATTEMPTS,
    message = "Превышено максимальное количество попыток ввода кода. Запросите код заново.",
    httpStatus = HttpStatusCode.BadRequest
)

class EmptyEmailException : AuthException(
    code = AuthErrorCode.AUTH_EMPTY_EMAIL,
    message = "Поле email обязательно для заполнения.",
    httpStatus = HttpStatusCode.BadRequest
)

class EmptyPasswordException : AuthException(
    code = AuthErrorCode.AUTH_EMPTY_PASSWORD,
    message = "Поле пароля обязательно для заполнения.",
    httpStatus = HttpStatusCode.BadRequest
)

class InvalidEmailException() : AuthException(
    code = AuthErrorCode.AUTH_INVALID_EMAIL_FORMAT,
    message = "Некорректный email.",
    httpStatus = HttpStatusCode.BadRequest
)

class InvalidCredentialsException : AuthException(
    code = AuthErrorCode.AUTH_INVALID_CREDENTIALS,
    message = "Неверный email или пароль.",
    httpStatus = HttpStatusCode.Unauthorized
)

class LengthPasswordException() : AuthException(
    code = AuthErrorCode.AUTH_PASSWORD_INVALID_LENGTH,
    message = "Длина пароля должна быть от 8 до 25 символов.",
    httpStatus = HttpStatusCode.BadRequest
)

class DigitPasswordException() : AuthException(
    code = AuthErrorCode.AUTH_PASSWORD_NO_DIGIT,
    message = "Пароль должен содержать хотя бы одну цифру.",
    httpStatus = HttpStatusCode.BadRequest
)

class UppercasePasswordException() : AuthException(
    code = AuthErrorCode.AUTH_PASSWORD_NO_UPPERCASE,
    message = "Пароль должен содержать хотя бы одну заглавную букву.",
    httpStatus = HttpStatusCode.BadRequest
)

class LowercasePasswordException() : AuthException(
    code = AuthErrorCode.AUTH_PASSWORD_NO_LOWERCASE,
    message = "Пароль должен содержать хотя бы одну строчную букву.",
    httpStatus = HttpStatusCode.BadRequest
)

class OldPasswordInvalidException() : AuthException(
    code = AuthErrorCode.AUTH_OLD_PASSWORD_INVALID,
    message = "Неверно указан текущий пароль.",
    httpStatus = HttpStatusCode.BadRequest
)

/**
 * OTP
 */

class EmptyOtpCodeException : AuthException(
    code = AuthErrorCode.OTP_EMPTY_CODE,
    message = "Код подтверждения не может быть пустым.",
    httpStatus = HttpStatusCode.BadRequest
)

class EmptyVerificationSessionException : AuthException(
    code = AuthErrorCode.OTP_EMPTY_TOKEN,
    message = "Сессия верификации обязательна для подтверждения.",
    httpStatus = HttpStatusCode.BadRequest
)

class InvalidOtpCodeException : AuthException(
    code = AuthErrorCode.OTP_INVALID_CODE,
    message = "Неверный код верификации.",
    httpStatus = HttpStatusCode.BadRequest
)

class ExpiredTokenException : AuthException(
    code = AuthErrorCode.OTP_EXPIRED_TOKEN,
    message = "Срок действия сессии верификации истек.",
    httpStatus = HttpStatusCode.Gone
)
class InvalidTokenException : AuthException(
    code = AuthErrorCode.OTP_INVALID_TOKEN,
    message = "Некорректный токен верификации.",
    httpStatus = HttpStatusCode.BadRequest
)

/**
 * Сброс пароля
 */

class EmptyResetTokenException : AuthException(
    code = AuthErrorCode.RESET_TOKEN_INVALID,
    message = "Токен восстановления обязателен для изменения пароля.",
    httpStatus = HttpStatusCode.BadRequest
)

class InvalidResetTokenException : AuthException(
    code = AuthErrorCode.RESET_TOKEN_INVALID,
    message = "Некорректный токен восстановления.",
    httpStatus = HttpStatusCode.BadRequest
)

class ExpiredResetTokenException : AuthException(
    code = AuthErrorCode.RESET_TOKEN_EXPIRED,
    message = "Срок действия сессии восстановления истек.",
    httpStatus = HttpStatusCode.Gone
)

class InvalidResetCodeException : AuthException(
    code = AuthErrorCode.RESET_INVALID_CODE,
    message = "Неверный код восстановления.",
    httpStatus = HttpStatusCode.BadRequest
)