package core.errors

import com.wiwchat.shared.common.data.errors.ServerErrorCode
import io.ktor.http.HttpStatusCode

open class ServerException(
    val code: ServerErrorCode,
    override val message: String,
    val httpStatus: HttpStatusCode
) : Exception(message)

class TooManyRequestsException() : ServerException(
    code = ServerErrorCode.RATE_LIMIT_EXCEEDED,
    message = "Вы отправляете запросы слишком часто. Пожалуйста, подождите.",
    httpStatus = HttpStatusCode.TooManyRequests
)

class InternalServerErrorException() : ServerException(
    code = ServerErrorCode.SERVER_INTERNAL_ERROR,
    message = "Произошла непредвиденная ошибка на сервере. Пожалуйста, обратитесь в поддержку.",
    httpStatus = HttpStatusCode.InternalServerError
)

class NotFoundException : ServerException(
    code = ServerErrorCode.ROUTE_NOT_FOUND,
    message = "Маршрут не найден.",
    httpStatus = HttpStatusCode.NotFound
)

class InvalidRequestFormatException : ServerException(
    code = ServerErrorCode.BAD_REQUEST_BODY,
    message = "Неверный формат запроса. Проверьте отправленные данные.",
    httpStatus = HttpStatusCode.BadRequest
)

class InvalidIdentifierException : ServerException(
    code = ServerErrorCode.INVALID_IDENTIFIER_FORMAT,
    message = "Указан неверный формат идентификатора. Ожидается валидный UUID.",
    httpStatus = HttpStatusCode.BadRequest
)

// Для RESOURCE_NOT_FOUND, EDITING_TIME_EXPIRED, RESOURCE_ALREADY_EXISTS
class ResourceException(
    code: ServerErrorCode,
    message: String,
    httpStatus: HttpStatusCode
) : ServerException(
    code = code,
    message = message,
    httpStatus = httpStatus
)

// для UNAUTHORIZED, INVALID_CREDENTIALS, ACCESS_DENIED, ACCOUNT_BANNED
class SecurityException(
    code: ServerErrorCode,
    message: String,
    httpStatus: HttpStatusCode = HttpStatusCode.Unauthorized
) : ServerException(
    code = code,
    message = message,
    httpStatus = httpStatus
)
/**
 * Пример
 * throw ResourceNotFoundException("Пользователь не найден")
 * throw ResourceNotFoundException("Чат не найден")
 * throw ResourceNotFoundException("Сообщение уже удалено", ServerErrorCode.RESOURCE_NOT_FOUND)
 */