package core.plugins

import com.wiwchat.shared.common.data.dto.*
import com.wiwchat.shared.common.data.dto.enums.WebSocketSystemAction
import com.wiwchat.shared.core.user.data.dto.enums.UserOnlineStatus
import features.profile.domain.service.UserProfileService
import io.ktor.http.HttpHeaders
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



val activeSessions = ConcurrentHashMap<String, ConcurrentHashMap<String, DefaultWebSocketServerSession>>()

fun Application.configureWebsockets() {
    val userProfileService by inject<UserProfileService>()

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

                val rawToken = call.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")?.trim()
                    ?: UUID.randomUUID().toString()

                val userIdUUID = UUID.fromString(userIdStr)

                // Регистрируем конкретное устройство
                val userSessions = activeSessions.computeIfAbsent(userIdStr) { ConcurrentHashMap() }
                val wasOffline = userSessions.isEmpty()
                userSessions[rawToken] = this

                try {
                    // Ставим статус ONLINE в БД только при подключении ПЕРВОГО устройства
                    if (wasOffline) {
                        userProfileService.setUserOnline(userIdUUID)
                        println("[DEBUG] Пользователь $userIdStr теперь ONLINE")
                    }

                    for (frame in incoming) {
                        // Просто удерживаем соединение живым.
                        // Если от клиента прилетит фрейм закрытия, Ktor сам корректно выйдет из цикла.
                        if (frame is Frame.Close) break
                    }
                } catch (e: Exception) {
                    // Логируем системную ошибку сокета на сервере без раскрытия ПДн
                    println("[INFO]: Соединение WebSocket для пользователя $userIdStr было прервано: ${e.message}")
                } finally {
                    // Удаляем сессию текущего устройства
                    userSessions.remove(rawToken, this)

                    // Переводим в OFFLINE только если у пользователя не осталось ни одного активного окна/телефона
                    if (userSessions.isEmpty()) {
                        activeSessions.remove(userIdStr, userSessions)
                        val lastSeenMilli = userProfileService.setUserOffline(userIdUUID)
                        println("[DEBUG] Пользователь $userIdStr теперь OFFLINE. Last Seen: $lastSeenMilli")
                    }
                }
            }
        }
    }
}

/**
 * Выход пользователя из сессии
 */
suspend fun forceLogoutUser(
    userId: String,
    excludeAccessToken: String? = null
){
    val userSessions  = activeSessions[userId] ?: return

    val messageObj = WebSocketSystemDto(
        action = WebSocketSystemAction.FORCE_LOGOUT,
        reason = WebsocketsErrorMessages.WS_FORCE_LOGOUT_REASON
    )
    val jsonText = Json.encodeToString(messageObj)

    userSessions.forEach { (token, session) ->
        // Пропускаем инициатора смены пароля
        if (token == excludeAccessToken) return@forEach

        try {
            session.send(Frame.Text(jsonText))
            session.close(
                CloseReason(
                    CloseReason.Codes.NORMAL,
                    WebsocketsErrorMessages.LOG_WS_CLOSE_FORCE_LOGOUT
                )
            )
        } catch (e: Exception) {
            // Игнорируем ошибку, если сокет уже нет
        } finally {
            userSessions.remove(token, session)
        }
    }

    if (userSessions.isEmpty()) {
        activeSessions.remove(userId, userSessions)
    }

}

/**
 * Принудительное выкидывание заблокированного пользователя по WebSocket
 */
suspend fun suspendUserWebSocket(userId: String) {
    val userSessions  = activeSessions[userId] ?: return

    val messageObj = WebSocketSystemDto(
        action = WebSocketSystemAction.ACCOUNT_SUSPENDED,
        reason = WebsocketsErrorMessages.WS_ACCOUNT_SUSPENDED_REASON
    )
    val jsonText = Json.encodeToString(messageObj)

    userSessions.forEach { (token, session) ->
        try {
            session.send(Frame.Text(jsonText))
            session.close(
                CloseReason(
                    CloseReason.Codes.VIOLATED_POLICY,
                    WebsocketsErrorMessages.LOG_WS_CLOSE_SUSPENDED
                )
            )
        } catch (e: Exception) {
            // Игнорируем, если сокет уже отвалился
        } finally {
            userSessions.remove(token, session)
        }
    }

    if (userSessions.isEmpty()) {
        activeSessions.remove(userId, userSessions)
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