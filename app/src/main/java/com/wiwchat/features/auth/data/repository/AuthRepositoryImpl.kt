package com.wiwchat.features.auth.data.repository

import com.wiwchat.shared.common.data.local.dao.SessionDao
import com.wiwchat.shared.common.data.local.dao.UserDao
import com.wiwchat.shared.common.data.local.entity.SessionEntity
import com.wiwchat.core.data.local.store.AuthDataStore
import javax.inject.Inject
import javax.inject.Singleton
import com.wiwchat.shared.common.data.local.entity.UserEntity
import com.wiwchat.shared.common.domain.model.DomainResult
import com.wiwchat.shared.common.data.network.util.safeApiCall
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

    override suspend fun verifyOtp(email: String, code: String, registrationSessionId: String): DomainResult<LoginResponse> {
        return safeApiCall<LoginResponse> {
            authApi.verifyOtp(VerifyOtpRequest(email, code, registrationSessionId))
        }
    }

    override suspend fun resendOtp(email: String, registrationSessionId: String): DomainResult<RegisterResponse> {
        return safeApiCall<RegisterResponse> {
            authApi.resendOtp(
                ResendOtpRequest(
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

    override suspend fun forgotPassword(email: String): DomainResult<ForgotPasswordResponse> {
        return safeApiCall<ForgotPasswordResponse> {
            authApi.forgotPassword(ForgotPasswordRequest(email = email))
        }
    }

    override suspend fun verifyResetOtp(
        email: String,
        code: String,
        resetToken: String
    ): DomainResult<String> {
        val result = safeApiCall<ForgotPasswordResponse> {
            authApi.verifyResetOtp(
                VerifyResetOtpRequest(
                    email = email,
                    code = code,
                    resetToken = resetToken
                )
            )
        }

        return when (result) {
            is DomainResult.Success -> DomainResult.Success(result.value.resetToken)
            is DomainResult.Failure -> DomainResult.Failure(result.error)
        }
    }

    override suspend fun resetPassword(
        email: String,
        resetToken: String,
        newPassword: String
    ): DomainResult<Unit> {
        val result = safeApiCall<StatusResponse> {
            authApi.resetPassword(
                ResetPasswordRequest(
                    email = email,
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

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): DomainResult<LoginResponse> {
        val result = safeApiCall<LoginResponse> {
            authApi.changePassword(
                ChangePasswordRequest(
                    oldPassword = oldPassword,
                    newPassword = newPassword
                )
            )
        }

        if (result is DomainResult.Success) {
            val responseBody = result.value
            val currentEmail = userDao.getUserById(responseBody.userId)?.email.orEmpty()
            saveSession(responseBody, currentEmail)
        }

        return result
    }

    override suspend fun logout(): DomainResult<Unit> {
        val currentRefreshToken = sessionDao.getSessionFirst()?.refreshToken

        val result = if (!currentRefreshToken.isNullOrBlank()) {
            when (val apiResult = safeApiCall<StatusResponse> {
                authApi.logout(RefreshRequest(refreshToken = currentRefreshToken))
            }) {
                is DomainResult.Success -> DomainResult.Success(Unit)
                is DomainResult.Failure -> DomainResult.Failure(apiResult.error)
            }
        } else {
            DomainResult.Success(Unit)
        }

        clearSession()

        return when (result) {
            is DomainResult.Success -> DomainResult.Success(Unit)
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
            displayName = response.userProfile.displayName,
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