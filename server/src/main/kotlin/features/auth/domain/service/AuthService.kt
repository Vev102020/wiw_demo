package features.auth.domain.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.wiwchat.shared.core.user.data.dto.enums.AccountStatus
import com.wiwchat.shared.core.user.data.dto.enums.UserOnlineStatus
import com.wiwchat.shared.core.user.data.dto.enums.UserRole
import com.wiwchat.shared.core.util.maskEmail
import com.wiwchat.shared.features.auth.data.dto.*
import com.wiwchat.shared.features.auth.data.dto.enums.OperationStatus
import org.jetbrains.exposed.sql.*
import org.mindrot.jbcrypt.BCrypt
import java.util.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import core.util.RedisManager
import features.auth.data.*
import com.wiwchat.shared.features.auth.domain.constants.AuthConstants.CODE_LENGTH
import com.wiwchat.shared.features.profile.data.dto.UserProfileDto
import com.wiwchat.shared.features.profile.domain.constants.ProfileConstants
import core.errors.AccountSuspendedException
import core.errors.ExpiredResetTokenException
import core.errors.ExpiredTokenException
import core.errors.InvalidCredentialsException
import core.errors.InvalidOtpCodeException
import core.errors.InvalidResetCodeException
import core.errors.InvalidResetTokenException
import core.errors.InvalidTokenException
import core.errors.OldPasswordInvalidException
import core.errors.TooManyOtpAttemptsException
import core.errors.TooManyRequestsException
import core.plugins.*
import features.auth.data.mapper.toAuthUser
import features.auth.data.mapper.toUserProfileDto
import features.auth.domain.model.AuthUser
import features.profile.data.table.UserProfilesTable
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class AuthService(
    private val jwtAlgorithm: Algorithm,
    private val jwtIssuer: String,
    private val jwtAudience: String,
    private val emailService: EmailService
) {
    /**
     * Создаем Scope для фоновых задач отправки писем (IO пул потоков)
     */
    private val mailScope = CoroutineScope(Dispatchers.IO)

    private data class AuthSessionState(
        val email: String,
        val code: String,
        val attempts: Int,
        val passwordHash: String? = null
    )

    /**
     * Админ
     * Глобальная блокировка пользователя на платформе
     */
    suspend fun suspendUser(targetUserId: UUID): StatusResponse {
        // Асинхронно обновляем статус аккаунта в БД
        newSuspendedTransaction(Dispatchers.IO) {
            UsersTable.update({ UsersTable.id eq targetUserId }) {
                it[this.accountStatus] = AccountStatus.SUSPENDED
            }

            // По требованиям ИБ: мгновенно уничтожаем все сессии устройства этого юзера в Postgres
            UserSessionsTable.deleteWhere { userId eq targetUserId }
        }

        // Мгновенно выкидываем его сессию из кэша Redis (чтобы refresh token больше не работал)
        val userIdStr = targetUserId.toString()
        RedisManager.deleteSession(userIdStr)
        // Если пользователь сейчас онлайн, его приложение мгновенно закроется баннером блокировки.
        suspendUserWebSocket(userIdStr)

        return StatusResponse(
            status = OperationStatus.SUCCESS.name.lowercase(),
            message = "Пользователь успешно заблокирован. Все активные сессии принудительно завершены."
        )
    }

    /**
     *  Снятие ограничений и активация аккаунта
     */
    suspend fun activateUser(targetUserId: UUID): StatusResponse {
        newSuspendedTransaction(Dispatchers.IO) {
            UsersTable.update({ UsersTable.id eq targetUserId }) {
                it[this.accountStatus] = AccountStatus.ACTIVE
            }
        }

        return StatusResponse(
            status = OperationStatus.SUCCESS.name.lowercase(),
            message = "Аккаунт пользователя успешно активирован."
        )
    }

    /**
     * Изменение глобальной роли пользователя (Назначение админом / Снятие прав)
     */
    suspend fun updateUserRole(targetUserId: UUID, newRole: UserRole): StatusResponse {
        newSuspendedTransaction(Dispatchers.IO) {
            UsersTable.update({ UsersTable.id eq targetUserId }) {
                it[this.role] = newRole
            }

            // Безопасность: при изменении уровня доступа (прав) сбрасываем старые сессии,
            // чтобы пользователь перелогинился и получил новый JWT-токен уже с новой ролью внутри!
            UserSessionsTable.deleteWhere { userId eq targetUserId }
        }

        RedisManager.deleteSession(targetUserId.toString())

        return StatusResponse(
            status = OperationStatus.SUCCESS.name.lowercase(),
            message = "Глобальная роль пользователя успешно изменена на ${newRole.name}. Сессии обновлены."
        )
    }

    /**
     * Пред-регистрация. Валидирует данные и выпускает шифрованный JWT токен.
     */
    suspend fun register(request: RegisterRequest): RegisterResponse {
        val cleanEmail = request.email.trim().lowercase()

        //Запрещаем отправлять письма чаще, чем раз в 60 секунд на один email.
        if (RedisManager.isRateLimited("register:$cleanEmail", expireSeconds = 60)) {
            throw TooManyRequestsException()
        }

        // Проверяем, не занят ли email
        val userExists = newSuspendedTransaction(Dispatchers.IO)  {
            UsersTable.selectAll().where { UsersTable.email eq cleanEmail }.any()
        }

        // Хэшируем пароль (для существующего пользователя берем случайную строку, чтобы BCrypt работал столько же по времени)
        val passwordToHash = if (userExists) Long.MAX_VALUE.toString() else request.password
        val hashedPassword = BCrypt.hashpw(passwordToHash, BCrypt.gensalt(10))

        if (userExists) {
            // ИБ Best Practice: Не выдаем злоумышленнику существование email в базе.
            // Отправляем alert-письмо о попытке повторной регистрации на почту асинхронно.
            mailScope.launch {
                emailService.sendAccountAlreadyExistsAlert(cleanEmail)
            }
            // Возвращаем фейковый успешный ID сессии. Злоумышленник думает, что всё ок,
            // а легитимный пользователь проверит почту и увидит предупреждение.
            return RegisterResponse(
                registrationSessionId = generateSessionId(),
                status = OperationStatus.PENDING.name.lowercase()
            )
        }

        // Генерируем OTP
        val generatedCode = generateOtpCode()
        val sessionId = generateSessionId()

        // Упаковываем все данные во временный JSON-стейт для Redis
        val sessionState = buildJsonObject {
            put("email", cleanEmail)
            put("passwordHash", hashedPassword) // Хэш лежит изолированно внутри Docker-сети Redis
            put("code", generatedCode)
            put("attempts", 0) // Счетчик будущих ошибок ввода OTP для защиты от брутфорса
        }.toString()

        // Сохраняем в Redis на 5 минут (300 секунд)
        RedisManager.saveRegistrationSession(sessionId, sessionState, ttlSeconds = 300)

        // Отправка письма (Асинхронно, как у вас и было)
        mailScope.launch {
            try {
                emailService.sendRegistrationOtp(cleanEmail, generatedCode)
            } catch (e: Exception) {
                // Логируем ошибку почтового провайдера, чтобы сервер не упал
                println("[ERROR]: Не удалось отправить регистрационный OTP. Сессия: $sessionId. Ошибка: ${e.message}")
            }
        }

        return RegisterResponse(
            registrationSessionId = sessionId,
            status = OperationStatus.PENDING.name.lowercase()
        )
    }

    /**
     * Верификация OTP. Расшифровывает токен и совершает ОДНУ запись в БД
     */

    suspend fun verifyOtp(request: VerifyEmailRequest, userAgent: String): LoginResponse {
        val sessionId = request.registrationSessionId

        val sessionState = try {
            getAndValidateSession(sessionId, request.email, isResetPassword = false)
        } catch (e: Exception) {
            throw InvalidOtpCodeException()
        }

        val passwordHash = sessionState.passwordHash ?: throw InvalidTokenException()

        // Если клиент уже ошибся 3 раза — полностью уничтожаем сессию в Redis
        if (sessionState.attempts >= 3) {
            RedisManager.deleteRegistrationSession(sessionId)
            throw TooManyOtpAttemptsException()
        }

        if (sessionState.code != request.code) {
            val nextAttempts = sessionState.attempts + 1
            val currentTtl = RedisManager.getRegistrationTtl(sessionId)

            val updatedSession = buildJsonObject {
                put("email", sessionState.email)
                put("passwordHash", passwordHash)
                put("code", sessionState.code)
                put("attempts", nextAttempts)
            }.toString()

            if (currentTtl > 0) {
                RedisManager.saveRegistrationSession(sessionId, updatedSession, currentTtl)
            }
            throw InvalidOtpCodeException()
        }

        // Защита от race condition (проверяем, не успели ли занять email за эти 5 минут)
        val userExists = newSuspendedTransaction(Dispatchers.IO)  {
            UsersTable.selectAll().where { UsersTable.email eq sessionState.email }.any()
        }
        if (userExists) {
            RedisManager.deleteRegistrationSession(sessionId)
            throw InvalidTokenException()
        }

        val emailVerifiedAt = Instant.now()
        val displayName = null
        val avatarUrl = null

        val (generatedId, randomUsername) = newSuspendedTransaction(Dispatchers.IO)  {
            val userInsert = UsersTable.insert {
                it[this.email] = sessionState.email
                it[this.passwordHash] = passwordHash
                it[this.emailVerifiedAt] = emailVerifiedAt
                it[this.role] = UserRole.USER
                it[this.accountStatus] = AccountStatus.ACTIVE
            }
            val userId = userInsert[UsersTable.id]

            val username = generateDefaultUsername(userId)

            val defaultSettingsJson = buildJsonObject {
                put("theme", "system")
                put("notifications", true)
            }.toString()

            UserProfilesTable.insert {
                it[this.userId] = userId
                it[this.username] = username
                it[this.displayName] = displayName
                it[this.status] = UserOnlineStatus.OFFLINE
                it[this.settings] = defaultSettingsJson
            }

            userId to username
        }

        // Удаляем временную сессию регистрации в Redis, так как она успешно завершена
        RedisManager.deleteRegistrationSession(sessionId)

        val userIdString = generatedId.toString()

        val safeProfile = UserProfileDto(
            id = userIdString,
            username = randomUsername,
            displayName = displayName,
            avatarUrl = avatarUrl,
            status = UserOnlineStatus.OFFLINE.name,
            lastSeen = emailVerifiedAt.toEpochMilli() // Переводим текущее системное время в Long миллисекунды для KMP-клиента
        )

        // Генерируем финальные токены доступа (Access/Refresh JWT для авторизованной зоны)
        val response = generateAuthTokens(
            userId = userIdString,
            role = UserRole.USER.name,
            emailVerifiedAt = emailVerifiedAt,
            userProfile = safeProfile
        )

        // Запись постоянной сессии авторизации в кеш Redis на 30 дней
        RedisManager.saveSession(
            userId = userIdString,
            refreshToken = response.refreshToken,
            ttlSeconds = 30 * 86400L
        )

        // Пишем сессию устройства в БД
        newSuspendedTransaction(Dispatchers.IO)  {
            UserSessionsTable.insert {
                it[this.userId] = generatedId
                it[this.refreshToken] = response.refreshToken
                it[this.deviceInfo] = userAgent
            }
        }

        // Отправляем сообщение о регистрации на почту
        mailScope.launch {
            try {
                emailService.sendWelcomeEmail(sessionState.email)
            } catch (e: Exception) {
                println("[ERROR]: Сбой отправки письма Welcome. Сессия: $sessionId. Ошибка: ${e.message}")
            }
        }

        return response

    }

    /**
     * Верификация повторная отправка
     */
    suspend fun resendRegistrationOtp(request: ResendCodeRequest): RegisterResponse {
        val sessionId = request.registrationSessionId

        // Извлекаем текущую сессию из Redis
        val sessionState = getAndValidateSession(sessionId, request.email, isResetPassword = false)
        val passwordHash = sessionState.passwordHash ?: throw InvalidTokenException()

        // Ограничение частоты повторного вызова (Rate Limiting)
        // Запрещаем генерировать новые коды чаще, чем раз в 60 секунд для конкретной сессии регистрации
        if (RedisManager.isRateLimited("resend:$sessionId", expireSeconds = 60)) {
            throw TooManyRequestsException()
        }

        // Генерируем НОВЫЙ OTP код взамен старого
        val newOtpCodeString = generateOtpCode()

        // Формируем обновленный JSON-стейт
        // Мы сохраняем исходный passwordHash и сбрасываем счетчик attempts на 0
        val updatedSession = buildJsonObject {
            put("email", sessionState.email)
            put("passwordHash", passwordHash)
            put("code", newOtpCodeString)
            put("attempts", 0)
        }.toString()

        // Перезаписываем сессию в Redis, обновляя её время жизни (TTL) еще на 5 минут
        RedisManager.saveRegistrationSession(sessionId, updatedSession, ttlSeconds = 300)

        // Отправляем новый код на почту (Асинхронно, в IO-пуле, чтобы бэкенд не ждал отправки)
        mailScope.launch {
            try {
                emailService.sendRegistrationOtp(sessionState.email, newOtpCodeString)
            } catch (e: Exception) {
                println("[ERROR]: Сбой при отправке OTP. Сессия: $sessionId. Ошибка: ${e.message}")
            }
        }

        return RegisterResponse(
            registrationSessionId = sessionId,
            status = OperationStatus.PENDING.name.lowercase()
        )
    }

    /**
     * Авторизация (Логин) пользователя
     */
    suspend fun login(request: LoginRequest, userAgent: String): LoginResponse {
        val cleanEmail = request.email.trim().lowercase()

        // Извлекаем сгруппированные данные через AuthUser
        val user: AuthUser? = newSuspendedTransaction(Dispatchers.IO) {
            (UsersTable innerJoin UserProfilesTable)
                .selectAll()
                .where { UsersTable.email eq cleanEmail }
                .singleOrNull()
                ?.toAuthUser()
        }

        if (!checkPasswordSecurely(request.password, user) || user == null) {
            throw InvalidCredentialsException()
        }

        // Проверка статуса
        when (user.accountStatus) {
            AccountStatus.SUSPENDED -> throw AccountSuspendedException()
            AccountStatus.DELETED -> throw InvalidCredentialsException()
            else -> { /* Доступ разрешен */ }
        }

        // Сборка ответа и генерация токенов
        val userIdStr = user.id.toString()
        val response = generateAuthTokens(
            userId = userIdStr,
            role = user.role,
            userProfile = user.toUserProfileDto()
        )

        // Запись сессий
        RedisManager.saveSession(userIdStr, response.refreshToken, 30 * 86400L)
        newSuspendedTransaction(Dispatchers.IO) {
            UserSessionsTable.insert {
                it[this.userId] = user.id
                it[this.refreshToken] = response.refreshToken
                it[this.deviceInfo] = userAgent
            }
        }

        return response
    }

    /**
     * Запрос на восстановление пароля
     */
    suspend fun forgotPassword(request: ForgotPasswordRequest) : ForgotPasswordResponse {
        val cleanEmail = request.email.trim().lowercase()

        // Защита от флуда письмами: не чаще раза в минуту на этот email
        if (RedisManager.isRateLimited("forgot:$cleanEmail", expireSeconds = 60)) {
            throw TooManyRequestsException()
        }

        val userRow = newSuspendedTransaction(Dispatchers.IO) {
            UsersTable.selectAll().where { UsersTable.email eq cleanEmail }.singleOrNull()
        }

        val resetSessionId = generateSessionId()

        if (userRow == null) {
            return ForgotPasswordResponse(resetToken = resetSessionId)
        }

        val otpCodeString = generateOtpCode()

        // Кладем стейт сброса в Redis
        val resetState = buildJsonObject {
            put("email", cleanEmail)
            put("code", otpCodeString)
            put("attempts", 0)
        }.toString()

        RedisManager.saveRegistrationSession(resetSessionId, resetState, ttlSeconds = 300) // Используем редис-метод с TTL

        mailScope.launch {
            try {
                emailService.sendResetPasswordCode(cleanEmail, otpCodeString)
            } catch(e: Exception) {
                val safeEmail = maskEmail(cleanEmail)
                println("[ERROR]: Сбой отправки письма с кодом ВОССТАНОВЛЕНИЯ ПАРОЛЯ. Почта: $safeEmail. Ошибка: ${e.message}")
            }
        }

        return ForgotPasswordResponse(resetToken = resetSessionId)
    }

    /**
     * Сброс и сохранение нового пароля
     */
    suspend fun resetPassword(request: ResetPasswordRequest) {
        val sessionId = request.resetToken

        // Используем общую приватную функцию валидации
        val sessionState = getAndValidateSession(sessionId, request.email, isResetPassword = true)

        // Защита от брутфорса кода восстановления
        if (sessionState.attempts >= 3) {
            RedisManager.deleteRegistrationSession(sessionId)
            throw TooManyOtpAttemptsException()
        }

        if (sessionState.code != request.code) {
            val nextAttempts = sessionState.attempts + 1
            val currentTtl = RedisManager.getRegistrationTtl(sessionId)

            val updatedSession = buildJsonObject {
                put("email", sessionState.email)
                put("code", sessionState.code)
                put("attempts", nextAttempts)
            }.toString()

            if (currentTtl > 0) {
                RedisManager.saveRegistrationSession(sessionId, updatedSession, currentTtl)
            }
            throw InvalidResetCodeException()
        }

        // Хэшируем НОВЫЙ пароль (вычисляется только для подтвержденного юзера, DoS невозможен)
        val newPasswordHash = BCrypt.hashpw(request.newPassword, BCrypt.gensalt(10))

        val userIdUUID = newSuspendedTransaction(Dispatchers.IO) {
            val userRow = UsersTable
                .select(UsersTable.id)
                .where { UsersTable.email eq sessionState.email }
                .singleOrNull()

            if (userRow != null) {
                val id = userRow[UsersTable.id]

                // Обновляем пароль по точному UUID индексу
                UsersTable.update({ UsersTable.id eq id }) {
                    it[this.passwordHash] = newPasswordHash
                }
                id
            } else {
                null
            }
        }

        if (userIdUUID == null) {
            throw InvalidResetTokenException()
        }

        // Полная инвалидация старых сессий взломанного/обновленного аккаунта
        newSuspendedTransaction(Dispatchers.IO)  {
            UserSessionsTable.deleteWhere { userId eq userIdUUID }
        }

        val userIdStr = userIdUUID.toString()
        // Удаляем токены из кэша Redis
        RedisManager.deleteSession(userIdStr)
        RedisManager.deleteRegistrationSession(sessionId)

        // Все устройства (включая сессию взломщика, если она была открыта) тихо разлогинятся.
        forceLogoutUser(userIdStr)

        // Пишем письмо о сбросе пароля
        mailScope.launch {
            try {
                emailService.sendPasswordChangedNotification(sessionState.email)
            } catch (e: Exception) {
                val safeEmail = maskEmail(sessionState.email)
                println("[ERROR]: Сбой отправки письма о СБРОСЕ ПАРОЛЯ. Почта: $safeEmail. Ошибка: ${e.message}")
            }
        }
    }


    /**
     * Смена пароля изнутри настроек авторизованным пользователем
     */
    suspend fun changePassword(userId: UUID, request: ChangePasswordRequest, userAgent: String): LoginResponse {
        // Извлекаем пользователя из БД для проверки старого пароля
        val user: AuthUser = newSuspendedTransaction(Dispatchers.IO) {
            (UsersTable innerJoin UserProfilesTable)
                .selectAll()
                .where { UsersTable.id eq userId }
                .singleOrNull()
                ?.toAuthUser()
        } ?: throw InvalidTokenException()

        // Проверяем, совпадает ли старый пароль с хэшем из БД
        if (!BCrypt.checkpw(request.oldPassword, user.passwordHash)) {
            throw OldPasswordInvalidException()
        }

        // Хэшируем новый пароль
        val newPasswordHash = BCrypt.hashpw(request.newPassword, BCrypt.gensalt(10))

        // Обновляем пароль в базе данных
        newSuspendedTransaction(Dispatchers.IO) {
            UsersTable.update({ UsersTable.id eq userId }) {
                it[this.passwordHash] = newPasswordHash
            }
        }

        val userIdStr = userId.toString()

        // Генерируем новую пару токенов для текущего устройства
        val response = generateAuthTokens(
            userId = userIdStr,
            role = user.role,
            userProfile = user.toUserProfileDto()
        )

        // Полная инвалидация ВСЕХ старых сессий в Redis и БВ
        RedisManager.deleteSession(userIdStr)
        RedisManager.saveSession(userIdStr, response.refreshToken, ttlSeconds = 30 * 86400L)

        newSuspendedTransaction(Dispatchers.IO) {
            UserSessionsTable.deleteWhere { UsersTable.id eq userId }

            // Записываем только текущую, новую сессию
            UserSessionsTable.insert {
                it[this.userId] = userId
                it[this.refreshToken] = response.refreshToken
                it[this.deviceInfo] = userAgent
            }
        }

        // Пишем письмо о сбросе пароля
        mailScope.launch {
            try {
                emailService.sendPasswordChangedNotification(user.email)
            } catch (e: Exception) {
                val safeEmail = maskEmail(user.email)
                println("[ERROR]: Сбой отправки письма о СМЕНЕ ПАРОЛЯ. Почта: $safeEmail. Ошибка: ${e.message}")
            }
        }

        // Выход из аккаунта. Выбиваем все остальные устройства.
        forceLogoutUser(userIdStr)

        return response
    }

    /**
     * Обновление токена
     */
    suspend fun refresh(oldRefreshToken: String, userAgent: String): LoginResponse {

        val userIdStr = try {
            val decodedJWT = JWT.require(jwtAlgorithm)
                .build()
                .verify(oldRefreshToken)
            decodedJWT.subject ?: throw InvalidTokenException()
        } catch (e: Exception) {
            // Если токен изменен, просрочен или подделан
            throw InvalidTokenException()
        }

        val userIdUUID = UUID.fromString(userIdStr)

        // Если сессии в Redis нет ИЛИ токен в кэше не совпадает с тем, что прислал клиент — токен невалиден
        val cachedRefreshToken = RedisManager.getSession(userIdStr)
        if (cachedRefreshToken == null || cachedRefreshToken != oldRefreshToken) {
            throw InvalidTokenException()
        }

        // Проверяем актуальный статус аккаунт
        val user: AuthUser = newSuspendedTransaction(Dispatchers.IO) {
            (UsersTable innerJoin UserProfilesTable)
                .selectAll()
                .where { UsersTable.id eq userIdUUID }
                .singleOrNull()
                ?.toAuthUser()
        } ?: throw InvalidTokenException()

        // Проверяем, не забанил ли пользователя админ, пока приложение работало на фоне
        when (user.accountStatus) {
            AccountStatus.SUSPENDED -> throw AccountSuspendedException()
            AccountStatus.DELETED -> throw InvalidTokenException()
            else -> { /* Доступ разрешен */ }
        }

        // Создаем новую пару токенов
        val response = generateAuthTokens(
            userId = userIdStr,
            role = user.role,
            userProfile = user.toUserProfileDto()
        )

        // Обновление сессии
        RedisManager.deleteSession(userIdStr)
        RedisManager.saveSession(userIdStr, response.refreshToken, ttlSeconds = 30 * 86400L)

        newSuspendedTransaction(Dispatchers.IO) {
            UserSessionsTable.deleteWhere { (userId eq userIdUUID) and (refreshToken eq oldRefreshToken) }
            UserSessionsTable.insert {
                it[this.userId] = userIdUUID
                it[this.refreshToken] = response.refreshToken
                it[this.deviceInfo] = userAgent
            }
        }

        return response
    }

    /**
     * Выход из аккаунта
     */
    suspend fun logout(userId: UUID, refreshTokenStr: String): StatusResponse {
        val userIdStr = userId.toString()

        // Асинхронно удаляем именно текущую сессию устройства из БД
        newSuspendedTransaction(Dispatchers.IO) {
            UserSessionsTable.deleteWhere {
                (UserSessionsTable.userId eq userId) and (UserSessionsTable.refreshToken eq refreshTokenStr)
            }
        }

        // Очищаем рефреш-токен в Redis
        RedisManager.deleteSession(userIdStr)

        // Закрываем WebSocket соединение этого конкретного устройства
        // (Поскольку метод WebSocket close() отработает штатно, клиент поймет, что это запланированный выход)
        forceLogoutUser(userIdStr)

        return StatusResponse(
            status = OperationStatus.SUCCESS.name.lowercase(),
            message = "Вы успешно вышли из учетной записи. Сессия устройства завершена."
        )
    }

    /**
     * Генерация токена
     */
    private fun generateAuthTokens(
        userId: String,
        role: String,
        emailVerifiedAt: Instant? = null,
        userProfile: UserProfileDto
    ): LoginResponse {
        val accessToken = JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 900_000)) // 15 мин
            .sign(jwtAlgorithm)

        val refreshToken = JWT.create()
            .withIssuer(jwtIssuer)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 30 * 86_400_000L)) // 30 дней
            .sign(jwtAlgorithm)

        // Проверка нужна ли верификация
        val needsVerification = emailVerifiedAt == null

        return LoginResponse(
            userId = userId,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = 900,
            needsVerification = needsVerification,
            role = role,
            userProfile = userProfile
        )
    }


    private fun generateOtpCode(): String {
        val min = Math.pow(10.0, (CODE_LENGTH - 1).toDouble()).toInt() // 100000
        val max = Math.pow(10.0, CODE_LENGTH.toDouble()).toInt() - 1   // 999999
        return (min..max).random().toString()
    }

    private fun generateSessionId(): String {
        return UUID.randomUUID().toString()
    }

    private fun generateDefaultUsername(userId: UUID): String {
        // Берем хвостовую часть UUID высокой энтропии (например, 8 символов)
        val cryptoShard = userId.toString().replace("-", "").takeLast(8)

        // Берем текущее время в наносекундах и переводим в 36-ричную систему счисления (для компактности)
        // Это гарантирует, что даже у одного и того же UUID имя, сгенерированное в следующую наносекунду, будет отличаться
        val nanoTimeShard = System.nanoTime().toString(36).takeLast(6)

        return "user_$cryptoShard$nanoTimeShard"
    }

    private fun checkPasswordSecurely(password: String, user: AuthUser?): Boolean {
        // Если пользователя нет, берем валлидный по структуре фейковый хэш BCrypt
        val passwordHashInDb = user?.passwordHash
            ?: "\$2a\$10\$L2D6vMvPclwD8Y7mZ7mZ7uR7yvX3F7z8M6o9P1q2r3s4t5u6v7w8x"
        val isPasswordValid = BCrypt.checkpw(password, passwordHashInDb)

        return user != null && isPasswordValid
    }

    private fun getAndValidateSession(
        sessionId: String,
        requestEmail: String,
        isResetPassword: Boolean,
    ): AuthSessionState {
        // Получаем строку из Redis. Если её нет — вызываем переданное исключение
        val sessionDataString = RedisManager.getRegistrationSession(sessionId)
            ?: throw if (isResetPassword) ExpiredResetTokenException() else ExpiredTokenException()

        val session = try {
            Json.parseToJsonElement(sessionDataString).jsonObject
        } catch (e: Exception) {
            RedisManager.deleteRegistrationSession(sessionId)
            if (isResetPassword) throw InvalidResetTokenException() else throw InvalidTokenException()
        }

        val emailFromSession = session["email"]?.jsonPrimitive?.content
            ?: (if (isResetPassword) throw InvalidResetTokenException() else throw InvalidTokenException())

        val code = session["code"]?.jsonPrimitive?.content
            ?: (if (isResetPassword) throw InvalidResetTokenException() else throw InvalidTokenException())

        val attempts = session["attempts"]?.jsonPrimitive?.int
            ?: (if (isResetPassword) throw InvalidResetTokenException() else throw InvalidTokenException())

        val passwordHash = session["passwordHash"]?.jsonPrimitive?.content

        // Проверка подмены email. Если не совпал — это попытка подмены данных
        if (emailFromSession != requestEmail.trim().lowercase()) {
            if (isResetPassword) throw InvalidResetTokenException() else throw InvalidTokenException()
        }

        return AuthSessionState(emailFromSession, code, attempts, passwordHash)
    }
}