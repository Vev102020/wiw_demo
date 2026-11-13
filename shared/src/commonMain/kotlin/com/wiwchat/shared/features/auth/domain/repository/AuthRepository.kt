package com.wiwchat.shared.features.auth.domain.repository

import com.wiwchat.shared.common.domain.model.DomainResult
import com.wiwchat.shared.features.auth.data.dto.ForgotPasswordResponse
import com.wiwchat.shared.features.auth.data.dto.LoginResponse
import com.wiwchat.shared.features.auth.data.dto.RegisterResponse


interface AuthRepository {
    suspend fun register(email: String, password: String): DomainResult<RegisterResponse>

    suspend fun verifyOtp(email: String, code: String, registrationSessionId: String): DomainResult<LoginResponse>

    suspend fun resendOtp(email: String, registrationSessionId: String): DomainResult<RegisterResponse>

    suspend fun login(email: String, password: String): DomainResult<LoginResponse>
    suspend fun forgotPassword(email: String): DomainResult<ForgotPasswordResponse>
    suspend fun verifyResetOtp(email: String, code: String, resetToken: String): DomainResult<String>
    suspend fun resetPassword(email: String, resetToken: String, newPassword: String): DomainResult<Unit>
    suspend fun changePassword(oldPassword: String, newPassword: String): DomainResult<LoginResponse>
    suspend fun logout(): DomainResult<Unit>
    suspend fun saveSession(response: LoginResponse, email: String)

    suspend fun clearSession()
}