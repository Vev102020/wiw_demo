package core.plugins

import com.wiwchat.shared.common.data.dto.*
import com.wiwchat.shared.common.data.dto.enums.WebSocketSystemAction
import com.wiwchat.shared.core.user.data.dto.UserStatusEvent
import com.wiwchat.shared.core.user.data.dto.enums.UserOnlineStatus
import features.profile.domain.service.UserService
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.routing
import io.ktor.server.websocket.*
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import java.util.concurrent.ConcurrentHashMap
import io.ktor.websocket.Frame
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import java.util.UUID


object WebsocketsErrorMessages {
    // Для пользователей
    const val WS_ACCOUNT_SUSPENDED_REASON = "Ваш аккаунт был заблокирован администратором платформы."
    const val WS_FORCE_LOGOUT_REASON = "Сессия принудительно завершена сервером. Пожалуйста, войдите заново."

    // Для сетевых роботов, Nginx и DevOps-инженеров в логах трафика
    const val LOG_WS_CLOSE_UNAUTHORIZED = "Unauthorized WebSocket connection attempt"
    const val LOG_WS_CLOSE_SUSPENDED = "Account suspended by administrator"
    const val LOG_WS_CLOSE_FORCE_LOGOUT = "Session terminated by server force logout"
}


val activeSessions = ConcurrentHashMap<String, WebSocketServerSession>()

fun Application.configureWebsockets() {
    val userService by inject<UserService>()

    install(WebSockets) {
        pingPeriodMillis = 15_000
        timeoutMillis = 30_000
    }

    routing {
        authenticate("auth-jwt") {
            webSocket("/ws/session") {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                    ?: return@webSocket close(
                        CloseReason(
                            CloseReason.Codes.VIOLATED_POLICY,
                            WebsocketsErrorMessages.LOG_WS_CLOSE_UNAUTHORIZED
                        )
                    )

                val userIdUUID = UUID.fromString(userIdStr)
                activeSessions[userIdStr] = this

                try {
                    userService.setUserOnline(userIdUUID)
                    println("[DEBUG] Пользователь $userIdStr теперь ONLINE")

                    for (frame in incoming) {
                        // Просто удерживаем соединение живым.
                        // Если от клиента прилетит фрейм закрытия, Ktor сам корректно выйдет из цикла.
                        if (frame is Frame.Close) break
                    }
                } catch (e: Exception) {
                    // Логируем системную ошибку сокета на сервере без раскрытия ПДн
                    println("[INFO]: Соединение WebSocket для пользователя $userIdStr было прервано: ${e.message}")
                } finally {
                    // Удаляем сессию из карты ТОЛЬКО если это именно НАШЕ соединение (защита от Race Condition)
                    activeSessions.remove(userIdStr, this)
                    // Пользователь вышел из сети (при любом обрыве связи)
                    val lastSeenMilli = userService.setUserOffline(userIdUUID)
                    println("[DEBUG] Пользователь $userIdStr теперь OFFLINE. Last Seen: $lastSeenMilli")
                }
            }
        }
    }
}

suspend fun forceLogoutUser(userId: String){
    val session = activeSessions[userId]
    if (session != null) {
        try {
            val messageObj = WebSocketSystemDto(
                action = WebSocketSystemAction.FORCE_LOGOUT,
                reason = WebsocketsErrorMessages.WS_FORCE_LOGOUT_REASON
            )
            val jsonText = Json.encodeToString(messageObj)

            session.send(Frame.Text(jsonText))
            session.close(
                CloseReason(
                CloseReason.Codes.NORMAL,
                WebsocketsErrorMessages.LOG_WS_CLOSE_FORCE_LOGOUT
                )
            )
        } catch (e: Exception) {
            // Если сокет уже был мертв, просто игнорируем ошибку отправки
        } finally {
            activeSessions.remove(userId, session)
        }
    }
}

/**
 * Принудительное выкидывание заблокированного пользователя по WebSocket
 */
suspend fun suspendUserWebSocket(userId: String) {
    val session = activeSessions[userId]
    if (session != null) {
        try {
            // Собираем системный DTO с экшеном блокировки аккаунта
            val messageObj = WebSocketSystemDto(
                action = WebSocketSystemAction.ACCOUNT_SUSPENDED,
                reason = WebsocketsErrorMessages.WS_ACCOUNT_SUSPENDED_REASON
            )
            val jsonText = Json.encodeToString(messageObj)

            // Отправляем фрейм клиенту
            session.send(Frame.Text(jsonText))

            // Закрываем сокет с кодом нарушения политики безопасности
            session.close(
                CloseReason(
                CloseReason.Codes.VIOLATED_POLICY,
                WebsocketsErrorMessages.LOG_WS_CLOSE_SUSPENDED)
            )
        } catch (e: Exception) {
            // Если сокет уже отвалился до отправки — просто игнорируем ошибку
        } finally {
            // Удаляем из мапы активных соединений
            activeSessions.remove(userId, session)
        }
    }
}

/**
 * Рассылка изменения статуса всем, кто онлайн и находится в общих чатах
 */

suspend fun broadcastStatusEvent(senderId: String, currentStatus: UserOnlineStatus, lastSeenTime: Long) {
    // Ищем в БД ID всех участников, у которых есть общие комнаты с senderId
//    val peerIds: List<String> = chatService.getContactIdsForUser(senderId)
//
//    // Формируем событие изменения статуса
//    val eventJson = Json.encodeToString(
//        UserStatusEvent(userId = senderId, status = currentStatus, lastSeen = lastSeenTime)
//    )
//
//    // Бежим циклом по друзьям и, если они онлайн, отправляем им фрейм
//    for (peerId in peerIds) {
//        val session = activeSessions[peerId]
//        if (session != null) {
//            try {
//                session.send(Frame.Text(eventJson))
//            } catch (e: Exception) { /* Игнорируем мертвые сокеты */ }
//        }
//    }
}