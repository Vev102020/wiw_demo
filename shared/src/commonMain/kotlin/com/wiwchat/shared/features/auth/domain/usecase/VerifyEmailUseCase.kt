package com.wiwchat.shared.features.auth.domain.usecase

import com.wiwchat.shared.common.data.network.AppError
import com.wiwchat.shared.common.data.network.DomainResult
import com.wiwchat.shared.features.auth.domain.repository.AuthRepository
import jakarta.inject.Inject

class VerifyEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, code: String, preRegistrationToken: String): VerifyEmailResult {
        return when (val result = authRepository.verifyEmail(email, code, preRegistrationToken)) {
            is DomainResult.Success -> {
                val responseBody = result.value

                // сохраняем сессию
                authRepository.saveSession(responseBody, email)

                VerifyEmailResult.Success
            }
            is DomainResult.Failure -> {
                VerifyEmailResult.Error(result.error)
            }
        }
    }
}

sealed interface VerifyEmailResult {
    data object Success : VerifyEmailResult
    data class Error(val error: AppError) : VerifyEmailResult
}