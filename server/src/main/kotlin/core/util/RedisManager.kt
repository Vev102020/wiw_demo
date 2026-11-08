package core.util

import io.ktor.server.config.*
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.params.SetParams

object RedisManager {
    private lateinit var pool: JedisPool

    fun init(config: ApplicationConfig) {
        // Сначала проверяем переменную окружения Docker, если пусто — берем из application.conf
        val host = System.getenv("REDIS_HOST")
            ?: config.propertyOrNull("redis.host")?.getString()
            ?: "localhost"

        val port = System.getenv("REDIS_PORT")?.toInt()
            ?: config.propertyOrNull("redis.port")?.getString()?.toInt()
            ?: 6379

        val poolConfig = JedisPoolConfig().apply {
            maxTotal = 20 // Максимальное количество одновременных подключений
            maxIdle = 5
            minIdle = 2
        }
        pool = JedisPool(poolConfig, host, port)
    }

    /**
     * Функция для сохранения сессии с автоматическим временем жизни (TTL)
     */
    fun saveSession(userId: String, refreshToken: String, ttlSeconds: Long) {
        pool.resource.use { jedis ->
            val key = "session:$userId"
            jedis.set(key, refreshToken)
            jedis.expire(key, ttlSeconds)
        }
    }

    /**
     * Функция для проверки, валидна ли сессия
     */
    fun getSession(userId: String): String? {
        pool.resource.use { jedis ->
            return jedis.get("session:$userId")
        }
    }

    /**
     * Функция удаления сессии
     */
    fun deleteSession(userId: String) {
        pool.resource.use { jedis ->
            jedis.del("session:$userId")
        }
    }

    /**
     * Сохранить или обновить регистрационную сессию
     */
    fun saveRegistrationSession(sessionId: String, dataJson: String, ttlSeconds: Long) {
        pool.resource.use { jedis ->
            jedis.setex("reg_session:$sessionId", ttlSeconds, dataJson)
        }
    }

    /**
     * Получает JSON-строку временного состояния регистрации по ID сессии.
     */
    fun getRegistrationSession(sessionId: String): String? {
        pool.resource.use { jedis ->
            return jedis.get("reg_session:$sessionId")
        }
    }

    /**
     * Удаляет временную сессию регистрации (нужно при успешном OTP или при блокировке брутфорса).
     */
    fun deleteRegistrationSession(sessionId: String) {
        pool.resource.use { jedis ->
            jedis.del("reg_session:$sessionId")
        }
    }

    /**
     * Возвращает оставшееся время жизни (TTL) сессии регистрации в секундах.
     */
    fun getRegistrationTtl(sessionId: String): Long {
        pool.resource.use { jedis ->
            return jedis.ttl("reg_session:$sessionId")
        }
    }

    /**
     * Защита от флуда (Rate Limiting).
     * Проверяет, существует ли указанный ключ (например, IP или Email).
     * Если не существует — создает его на время [expireSeconds] и возвращает FALSE (лимит не превышен).
     * Если ключ уже существует — возвращает TRUE (запрос заблокирован лимитером).
     */
    fun isRateLimited(actionKey: String, expireSeconds: Long): Boolean {
        pool.resource.use { jedis ->
            val fullKey = "rate_limit:$actionKey"

            // Параметр NX означает: "записать только если ключа еще нет"
            // Параметр EX задает время жизни в секундах
            val setParams = SetParams().nx().ex(expireSeconds)
            val result = jedis.set(fullKey, "1", setParams)

            // Если Redis вернул null, значит ключ уже был в базе -> лимит сработал
            return result == null
        }
    }
}