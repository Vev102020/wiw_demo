package com.wiwchat.shared.features.auth.domain.usecase

import com.wiwchat.shared.common.domain.model.AppError
import com.wiwchat.shared.common.domain.model.DomainResult
import com.wiwchat.shared.features.auth.data.dto.enums.VerificationType
import com.wiwchat.shared.features.auth.domain.repository.AuthRepository
import jakarta.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        code: String,
        sessionToken: String,
        verifyType: VerificationType
    ): VerifyOtpResult {
        return when (verifyType) {
            VerificationType.REGISTRATION -> {
                when (val result = authRepository.verifyOtp(email, code, sessionToken)) {
                    is DomainResult.Success -> {
                        authRepository.saveSession(result.value, email)
                        VerifyOtpResult.RegistrationSuccess
                    }
                    is DomainResult.Failure -> VerifyOtpResult.Error(result.error)
                }
            }

            VerificationType.RESET_PASSWORD -> {
                when (val result = authRepository.verifyResetOtp(email, code, sessionToken)) {
                    is DomainResult.Success -> {
                        VerifyOtpResult.ResetCodeVerified(result.value)
                    }
                    is DomainResult.Failure -> VerifyOtpResult.Error(result.error)
                }
            }
        }
    }
}

sealed interface VerifyOtpResult {
    data object RegistrationSuccess : VerifyOtpResult
    data class ResetCodeVerified(val verifiedResetToken: String) : VerifyOtpResult
    data class Error(val error: AppError) : VerifyOtpResult
}