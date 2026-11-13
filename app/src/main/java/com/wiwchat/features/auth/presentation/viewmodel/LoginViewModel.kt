package com.wiwchat.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiwchat.shared.common.domain.errors.AuthErrorCode
import com.wiwchat.shared.common.domain.model.AppError
import com.wiwchat.shared.core.forms.FormFieldState
import com.wiwchat.shared.core.notification.NotificationManager
import com.wiwchat.shared.features.auth.domain.constants.AuthConstants
import com.wiwchat.shared.features.auth.domain.usecase.LoginResult
import com.wiwchat.shared.features.auth.domain.usecase.LoginUseCase
import com.wiwchat.shared.features.auth.domain.validator.AuthValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor (
    private val loginUseCase: LoginUseCase,
    private val notificationManager: NotificationManager
) : ViewModel() {

    val emailInput = FormFieldState(
        scope = viewModelScope,
        maxLength = AuthConstants.EMAIL_MAX_LENGTH,
        validator = { text -> AuthValidator.validateEmail(text) }
    )

    val passwordInput = FormFieldState(
        scope = viewModelScope,
        maxLength = AuthConstants.PASSWORD_MAX_LENGTH,
        validator = { text ->
            if (text.isBlank()) AuthErrorCode.AUTH_EMPTY_PASSWORD else null
        }
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val isButtonEnabled: StateFlow<Boolean> = combine(
        emailInput.isValid,
        passwordInput.isValid,
        _isLoading
    ) { isEmailValid, isPasswordValid, loading ->
        isEmailValid && isPasswordValid && !loading
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun loginToServer(onNavigateToVerification: (email: String) -> Unit) {
        if (!emailInput.validate() || !passwordInput.validate()) return

        viewModelScope.launch {
            _isLoading.value = true

            // Используем .value для чтения текущего текста из StateFlow в shared коде
            when (val result = loginUseCase(emailInput.text.value, passwordInput.text.value)) {
                is LoginResult.Success -> {
                    // Ничего делать не нужно: сессия уже сохранена внутри useCase
                }
                is LoginResult.NeedsVerification -> {
                    onNavigateToVerification(result.email)
                }
                is LoginResult.Error -> {
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
            AuthErrorCode.AUTH_INVALID_CREDENTIALS -> {
                emailInput.setErrorFromBackend(code)
                passwordInput.setErrorFromBackend(code)
            } else -> {
                notificationManager.showNotification(originalError)
            }
        }
    }


}