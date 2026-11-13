package com.wiwchat.shared.common.data.network.util

import com.wiwchat.shared.common.data.dto.ErrorResponse
import com.wiwchat.shared.common.domain.model.AppError
import com.wiwchat.shared.common.domain.model.DomainResult
import com.wiwchat.shared.common.domain.errors.*
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.network.sockets.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

private val errorRegistry: List<(String) -> ApiErrorCode?> = listOf(
    AuthErrorCode::find,
    ServerErrorCode::find,
    MediaErrorCode::find,
    ProfileErrorCode::find,
    ChatErrorCode::find,
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
        println("DEBUG_DIAGNOSIS: Поймали ClientRequestException (4xx)")
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
        println("DEBUG_DIAGNOSIS: Поймали ServerResponseException (5xx)")
        // Ловим ошибки 5xx сервера
        DomainResult.Failure(AppError.ServerUnreachable)
    } catch (e: ConnectTimeoutException) {
        println("DEBUG_DIAGNOSIS: Поймали ConnectTimeoutException -> ServerUnreachable")
        DomainResult.Failure(AppError.ServerUnreachable)
    } catch (e: SocketTimeoutException) {
        println("DEBUG_DIAGNOSIS: Поймали SocketTimeoutException -> ServerUnreachable")
        DomainResult.Failure(AppError.ServerUnreachable)
    } catch (e: IOException) {
        val message = e.message?.lowercase().orEmpty()
        println("DEBUG_DIAGNOSIS: Поймали IOException! Реальное сообщение: '$message'")
        when {
            // Если в ошибке есть маркеры закрытого порта локального хоста (Docker выключен)
            message.contains("failed to connect") ||
                    message.contains("failed to connect") ||
                    message.contains("refused") ||
                    message.contains("route to host") ||
                    message.contains("cleartext") -> {
                        DomainResult.Failure(AppError.ServerUnreachable)
                    }
            // Во всех остальных случаях — у пользователя действительно выключен интернет на телефоне
            else -> {
                DomainResult.Failure(AppError.NetworkUnavailable)
            }
        }
    } catch (e: Exception) {
        println("DEBUG_DIAGNOSIS: Поймали неизвестный Exception: ${e::class.simpleName} - ${e.message}")
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