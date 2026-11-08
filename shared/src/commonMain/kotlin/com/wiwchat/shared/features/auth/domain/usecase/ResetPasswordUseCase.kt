package com.wiwchat.shared.features.auth.domain.usecase

import com.wiwchat.shared.common.data.network.AppError
import com.wiwchat.shared.common.data.network.DomainResult
import com.wiwchat.shared.features.auth.domain.repository.AuthRepository
import jakarta.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        code: String,
        resetToken: String,
        newPassword: String
    ): ResetPasswordResult {
        return when (val result = authRepository.resetPassword(email, code, resetToken, newPassword)) {
            is DomainResult.Success -> {
                // Успех сброса пароля. Пользователя отправляем на экран Login.
                ResetPasswordResult.Success
            }
            is DomainResult.Failure -> {
                ResetPasswordResult.Error(result.error)
            }
        }
    }
}

sealed interface ResetPasswordResult {
    data object Success : ResetPasswordResult
    data class Error(val error: AppError) : ResetPasswordResult
}