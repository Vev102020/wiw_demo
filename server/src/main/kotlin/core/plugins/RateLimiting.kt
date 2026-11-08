package core.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.origin
import kotlin.time.Duration.Companion.seconds
import io.ktor.server.plugins.ratelimit.*

fun Application.configureRateLimiting() {
    // Устанавливаем глобальный плагин лимитов запросов (Rate Limit)
    install(RateLimit) {
        // Глобальное ограничение сервера (Защита от DDoS)
        register {
            rateLimiter(limit = 100, refillPeriod = 10.seconds)
            requestKey { call -> call.request.origin.remoteHost }
        }

        // Жесткое ограничение для Auth-окон (Защита от Брутфорса)
        register(RateLimitName("auth")) {
            rateLimiter(limit = 5, refillPeriod = 10.seconds)
            requestKey { call -> call.request.origin.remoteHost }
        }
    }
}
