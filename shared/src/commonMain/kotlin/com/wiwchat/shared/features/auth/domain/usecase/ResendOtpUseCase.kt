package com.wiwchat.shared.features.auth.domain.usecase

import com.wiwchat.shared.common.domain.model.AppError
import com.wiwchat.shared.common.domain.model.DomainResult
import com.wiwchat.shared.features.auth.data.dto.enums.VerificationType
import com.wiwchat.shared.features.auth.domain.repository.AuthRepository
import jakarta.inject.Inject

class ResendOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        currentSessionToken: String,
        verifyType: VerificationType
    ): ResendOtpResult {
        return when (verifyType) {
            VerificationType.REGISTRATION -> {
                when (val result = authRepository.resendOtp(email, currentSessionToken)) {
                    is DomainResult.Success -> ResendOtpResult.Success(result.value.registrationSessionId)
                    is DomainResult.Failure -> ResendOtpResult.Error(result.error)
                }
            }
            VerificationType.RESET_PASSWORD -> {
                when (val result = authRepository.forgotPassword(email)) {
                    is DomainResult.Success -> ResendOtpResult.Success(result.value.resetToken)
                    is DomainResult.Failure -> ResendOtpResult.Error(result.error)
                }
            }
        }
    }
}

sealed interface ResendOtpResult {
    data class Success(val newSessionToken: String) : ResendOtpResult
    data class Error(val error: AppError) : ResendOtpResult
}