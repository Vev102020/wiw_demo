package core.plugins

import com.wiwchat.shared.common.data.dto.ErrorResponse
import com.wiwchat.shared.common.data.errors.ServerErrorCode
import core.errors.AppException
import core.errors.InternalServerErrorException
import core.errors.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import io.ktor.server.plugins.BadRequestException

object StatusPagesErrorMessages {
    // Текст для пользователей
    const val MISSING_PARAMETERS_PREFIX = "Не переданы обязательные параметры: "
    const val INVALID_JSON_FORMAT = "Некорректный формат входных данных (невалидный JSON)"
}


@OptIn(ExperimentalSerializationApi::class)
fun Application.configureStatusPages() {
    install(StatusPages) {
        // неверный url
        status(HttpStatusCode.NotFound) { _, _ ->
            throw NotFoundException()
        }

        // неверный метод
        status(HttpStatusCode.MethodNotAllowed) { _, _ ->
            throw NotFoundException()
        }

        // Если переданы не все параметры (Плохой JSON, нехватка полей)
        exception<BadRequestException> { call, cause ->
            val origin = cause.cause

            val (errorCode, errorMessage) = when (origin) {
                is MissingFieldException -> {
                    val fields = origin.missingFields.joinToString()
                    ServerErrorCode.MISSING_REQUIRED_PARAMS to "${StatusPagesErrorMessages.MISSING_PARAMETERS_PREFIX}$fields"
                }
                else -> {
                    ServerErrorCode.BAD_REQUEST_BODY to StatusPagesErrorMessages.INVALID_JSON_FORMAT
                }
            }

            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(
                    code = errorCode.name,
                    message = errorMessage
                )
            )
        }

        // Ловим все наши контролируемые ошибки авторизации
        exception<AppException> { call, cause ->
            call.respond(
                status = cause.httpStatus,
                message = ErrorResponse(
                    code = cause.code.name,
                    message = cause.message
                )
            )
        }


        // Ловим любые другие непредвиденные падения (база данных, null pointer и т.д.)
        exception<Throwable> { call, cause ->
            println("[CRITICAL ERROR]: Системный сбой бэкенда: ${cause.stackTraceToString()}")

            InternalServerErrorException().run {
                call.respond(status = httpStatus, message = ErrorResponse(code = code.name, message = message))
            }
        }
    }
}
