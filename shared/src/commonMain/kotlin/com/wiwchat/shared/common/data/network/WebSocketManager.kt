package com.wiwchat.shared.common.data.network

import com.wiwchat.shared.common.data.dto.*
import com.wiwchat.shared.common.data.dto.enums.WebSocketSystemAction
import com.wiwchat.shared.common.domain.repository.TokenProvider
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

class WebSocketManager(
    private val client: HttpClient,
    private val tokenProvider: TokenProvider,
    private val serverUrl: String,
    private val serverPort: Int
) {
    private var webSocketJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob())

    fun connect(userId: String) {
        // Если соединение уже активно, ничего не делаем
        if (webSocketJob?.isActive == true) return

        webSocketJob = scope.launch {
            // Бесконечный цикл для автоматического переподключения при обрыве сети
            while (isActive) {
                try {

                    client.webSocket(
                        host = serverUrl,
                        port = serverPort,
                        path = "/ws/session"
                    ) {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()

                                try {
                                    // Пробуем распарсить системное сообщение из общей shared-модели
                                    val systemMessage = Json.decodeFromString<WebSocketSystemDto>(text)

                                    // Обрабатываем и принудительный выход, и блокировку аккаунта
                                    if (systemMessage.action == WebSocketSystemAction.FORCE_LOGOUT ||
                                        systemMessage.action == WebSocketSystemAction.ACCOUNT_SUSPENDED
                                    ) {
                                        tokenProvider.clearTokens()
                                        this.close()
                                        // Останавливаем корутину полностью, чтобы не было бесконечных реконнектов
                                        this@launch.cancel()
                                        return@webSocket
                                    }
                                } catch (e: Exception) {
                                    // Если пришел не JSON, а обычный текст (например, сообщение чата) — обрабатываем иначе
                                    println("[WS CLIENT] Получено обычное сообщение или неизвестный формат: $text")
                                }
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    // Обязательно пробрасываем отмену корутины
                    throw e
                } catch (e: Exception) {
                    // В случае падения сети ждем 5 секунд перед следующей попыткой
                    delay(5000.milliseconds)
                }
            }
        }
    }

    fun reconnect(userId: String) {
        disconnect()
        connect(userId)
    }

    fun disconnect() {
        webSocketJob?.cancel()
        webSocketJob = null
    }
}