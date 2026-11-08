package com.wiwchat.shared.features.auth.domain.repository

import com.wiwchat.shared.common.data.network.DomainResult
import com.wiwchat.shared.features.auth.data.dto.LoginResponse
import com.wiwchat.shared.features.auth.data.dto.RegisterResponse


interface AuthRepository {
    suspend fun register(email: String, password: String): DomainResult<RegisterResponse>

    suspend fun verifyEmail(email: String, code: String, registrationSessionId: String): DomainResult<LoginResponse>

    suspend fun resendCode(email: String, registrationSessionId: String): DomainResult<RegisterResponse>

    suspend fun login(email: String, password: String): DomainResult<LoginResponse>
    suspend fun forgotPassword(email: String): DomainResult<RegisterResponse>
    suspend fun resetPassword(email: String, code: String, resetToken: String, newPassword: String): DomainResult<Unit>

    suspend fun saveSession(response: LoginResponse, email: String)

    suspend fun clearSession()
}