package com.wiwchat.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiwchat.shared.common.domain.errors.AuthErrorCode
import com.wiwchat.shared.common.domain.model.AppError
import com.wiwchat.shared.common.domain.model.DomainResult
import com.wiwchat.shared.core.forms.FormFieldState
import com.wiwchat.shared.core.notification.NotificationManager
import com.wiwchat.shared.features.auth.domain.constants.AuthConstants
import com.wiwchat.shared.features.auth.domain.repository.AuthRepository
import com.wiwchat.shared.features.auth.domain.validator.AuthValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository : AuthRepository,
    private val notificationManager: NotificationManager
) : ViewModel() {
    val emailInput = FormFieldState(
        scope = viewModelScope,
        maxLength = AuthConstants.EMAIL_MAX_LENGTH,
        validator = { text -> AuthValidator.validateEmail(text) }
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Кнопка активна, если поле заполнено, нет локальных ошибок и не идет загрузка
    val isButtonEnabled: StateFlow<Boolean> = combine(
        emailInput.isValid,
        _isLoading
    ) { isEmailValid, loading ->
        isEmailValid && !loading
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun sendResetCode(onNavigateToResetVerification: (email: String, resetToken: String) -> Unit) {
        if (!emailInput.validate()) return

        viewModelScope.launch {
            _isLoading.value = true
            val cleanEmail = emailInput.text.value.trim()

            when (val result = authRepository.forgotPassword(cleanEmail)) {
                is DomainResult.Success -> {
                    val responseBody = result.value
                    // Переходим на экран ввода OTP и нового пароля
                    onNavigateToResetVerification(cleanEmail, responseBody.resetToken)
                }

                is DomainResult.Failure -> {
                    handleFailure(result.error)
                }
            }
            _isLoading.value = false
        }
    }


    private fun handleFailure(error: AppError) {
        if (error is AppError.Backend) {
            val authCode = error.code as? AuthErrorCode
            if (authCode != null) {
                handleBackendError(authCode, error)
            } else {
                notificationManager.showNotification(error)
            }
        } else {
            notificationManager.showNotification(error)
        }
    }

    private fun handleBackendError(code: AuthErrorCode, originalError: AppError.Backend) {
        when (code) {
            AuthErrorCode.AUTH_EMPTY_EMAIL,
            AuthErrorCode.AUTH_INVALID_EMAIL_FORMAT -> {
                emailInput.setErrorFromBackend(code)
            }
            else -> {
                notificationManager.showNotification(originalError)
            }
        }
    }

}