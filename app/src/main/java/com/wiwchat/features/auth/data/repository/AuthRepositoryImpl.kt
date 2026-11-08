package com.wiwchat.features.auth.data.repository

import com.wiwchat.shared.common.data.local.dao.SessionDao
import com.wiwchat.shared.common.data.local.dao.UserDao
import com.wiwchat.shared.common.data.local.entity.SessionEntity
import com.wiwchat.core.data.local.store.AuthDataStore
import javax.inject.Inject
import javax.inject.Singleton
import com.wiwchat.shared.common.data.local.entity.UserEntity
import com.wiwchat.shared.common.data.network.DomainResult
import com.wiwchat.shared.common.data.network.safeApiCall
import com.wiwchat.shared.features.auth.data.api.AuthApi
import com.wiwchat.shared.features.auth.data.dto.*
import com.wiwchat.shared.features.auth.domain.repository.AuthRepository


@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val authDataStore: AuthDataStore,
    private val userDao: UserDao,
    private val sessionDao: SessionDao,
) : AuthRepository {
    override suspend fun register(email: String, password: String): DomainResult<RegisterResponse> {
        return safeApiCall<RegisterResponse> {
            authApi.register(RegisterRequest(email = email, password = password))
        }
    }

    override suspend fun verifyEmail(email: String, code: String, registrationSessionId: String): DomainResult<LoginResponse> {
        return safeApiCall<LoginResponse> {
            authApi.verifyEmail(VerifyEmailRequest(email, code, registrationSessionId))
        }
    }

    override suspend fun resendCode(email: String, registrationSessionId: String): DomainResult<RegisterResponse> {
        return safeApiCall<RegisterResponse> {
            authApi.resendCode(
                ResendCodeRequest(
                    email = email,
                    registrationSessionId = registrationSessionId
                )
            )
        }
    }

    override suspend fun login(email: String, password: String): DomainResult<LoginResponse> {
        return safeApiCall<LoginResponse> {
            authApi.login(LoginRequest(email = email, password = password))
        }
    }

    override suspend fun forgotPassword(email: String): DomainResult<RegisterResponse> {
        return safeApiCall<RegisterResponse> {
            authApi.forgotPassword(ForgotPasswordRequest(email = email))
        }
    }

    override suspend fun resetPassword(
        email: String,
        code: String,
        resetToken: String,
        newPassword: String
    ): DomainResult<Unit> {
        val result = safeApiCall<StatusResponse> {
            authApi.resetPassword(
                ResetPasswordRequest(
                    email = email,
                    code = code,
                    resetToken = resetToken,
                    newPassword = newPassword
                )
            )
        }

        return when (result) {
            is DomainResult.Success<StatusResponse> -> DomainResult.Success(Unit)
            is DomainResult.Failure -> DomainResult.Failure(result.error)
        }
    }

    override suspend fun saveSession(
        response: LoginResponse,
        email: String
    ) {
        // очищаем старые сессии
        clearSession()

        // Сохраняем только системный ID
        authDataStore.saveUserId(response.userId)

        // Сохраняем профиль пользователя в локальную БД чата
        val userEntity = UserEntity(
            id = response.userId,
            email = email,
            username = null,
            role = response.role,
            isActive = true
        )
        userDao.insertUser(userEntity)

        // Сохраняем токены в Room
        val sessionEntity = SessionEntity(
            userId = response.userId,
            accessToken = response.accessToken,
            refreshToken = response.refreshToken
        )
        sessionDao.saveSession(sessionEntity)
    }

    override suspend fun clearSession() {
        authDataStore.clearTokens()
        userDao.clearUser()
        sessionDao.clearSession()
    }
}