package com.wiwchat.shared.common.data.network

import com.wiwchat.shared.common.data.dto.ErrorResponse
import com.wiwchat.shared.common.data.errors.*
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

private val errorRegistry: List<(String) -> ApiErrorCode?> = listOf(
    AuthErrorCode::find,
    ServerErrorCode::find,
    MediaErrorCode::find
)

suspend inline fun <reified T> safeApiCall(
    crossinline block: suspend () -> HttpResponse
): DomainResult<T> {
    return try {
        // Выполнение сетевого запроса и обработка результата
        val response = block()
        if (response.status.value in 200..299) {
            DomainResult.Success(response.body())
        } else {
            DomainResult.Failure(AppError.Unknown)
        }
    } catch (e: CancellationException) {
        // Обязательно пробрасываем отмену корутин
        throw e
    } catch (e: ClientRequestException) {
        // Ловим ошибки 4xx, пытаемся распарсить JSON с кодом ошибки
        val errorBody = try {
            Json.decodeFromString<ErrorResponse>(e.response.bodyAsText())
        } catch (_: Exception) {
            null
        }
        if (errorBody != null) {
            val typedCode = errorBody.code.toApiErrorCode()
            DomainResult.Failure(AppError.Backend(typedCode))
        } else {
            DomainResult.Failure(AppError.Unknown)
        }
    } catch (e: ServerResponseException) {
        // Ловим ошибки 5xx сервера
        DomainResult.Failure(AppError.ServerMaintenance)
    } catch (e: IOException) {
        // Проблемы с интернетом или таймауты соединения
        DomainResult.Failure(AppError.NetworkUnavailable)
    } catch (e: Exception) {
        DomainResult.Failure(AppError.Unknown)
    }
}

fun String.toApiErrorCode(): ApiErrorCode {
    for (lookup in errorRegistry) {
        val matchedCode = lookup(this)
        if (matchedCode != null) return matchedCode
    }
    return ServerErrorCode.SERVER_UNKNOWN_ERROR
}