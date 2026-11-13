package com.wiwchat.shared.features.auth.domain.usecase

import com.wiwchat.shared.common.domain.model.AppError
import com.wiwchat.shared.common.domain.model.DomainResult
import com.wiwchat.shared.features.auth.domain.repository.AuthRepository
import jakarta.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
){
    suspend operator fun invoke(email: String, password: String): LoginResult {
        val cleanEmail = email.trim()

        return when (val result = authRepository.login(cleanEmail, password)) {
            is DomainResult.Success -> {
                val responseBody = result.value

                if (!responseBody.needsVerification) {
                    authRepository.saveSession(responseBody, cleanEmail)

                    // ТУТ БУДЕТ ФОНОВАЯ СИНХРОНИЗАЦИЯ ЧАТОВ ПРИ СТАРТЕ:
                    // chatRepository.syncAllChatsWithServer(responseBody.userId)
                    // userRepository.syncContacts()

                    LoginResult.Success
                } else {
                    LoginResult.NeedsVerification(cleanEmail)
                }
            }
            is DomainResult.Failure -> {
                // Если бэкенд или сеть вернули ошибку, передаем её в UI
                LoginResult.Error(result.error)
            }
        }
    }
}

sealed interface LoginResult {
    data object Success : LoginResult
    data class NeedsVerification(val email: String) : LoginResult
    data class Error(val error: AppError) : LoginResult
}