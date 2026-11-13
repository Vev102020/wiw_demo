package com.wiwchat.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import com.wiwchat.shared.common.domain.errors.AuthErrorCode
import com.wiwchat.shared.common.domain.model.AppError
import com.wiwchat.shared.common.domain.model.DomainResult
import com.wiwchat.shared.core.forms.FormFieldState
import com.wiwchat.shared.core.notification.NotificationManager
import com.wiwchat.shared.features.auth.domain.constants.AuthConstants
import com.wiwchat.shared.features.auth.domain.repository.AuthRepository
import com.wiwchat.shared.features.auth.domain.validator.AuthValidator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.*


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationManager: NotificationManager,
) : ViewModel(){

    val emailInput = FormFieldState(
        scope = viewModelScope,
        maxLength = AuthConstants.EMAIL_MAX_LENGTH,
        validator = { text -> AuthValidator.validateEmail(text) }
    )

    val passwordInput = FormFieldState(
        scope = viewModelScope,
        maxLength = AuthConstants.PASSWORD_MAX_LENGTH,
        validator = { text -> AuthValidator.validatePassword(text) }
    )

    val passwordRepeatInput = FormFieldState(
        scope = viewModelScope,
        maxLength = AuthConstants.PASSWORD_MAX_LENGTH,
        validator = { text ->
            if (text != passwordInput.text.value) {
                AuthErrorCode.AUTH_PASSWORD_INVALID_LENGTH
            } else null
        }
    )

    private val _isPolicyChecked = MutableStateFlow(false)
    val isPolicyChecked: StateFlow<Boolean> = _isPolicyChecked.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val isButtonEnabled: StateFlow<Boolean> = combine(
        emailInput.isValid,
        passwordInput.isValid,
        passwordRepeatInput.isValid,
        _isPolicyChecked,
        _isLoading
    ) { emailValid: Boolean, passValid: Boolean, repeatValid: Boolean, policyChecked: Boolean, loading: Boolean ->
        emailValid && passValid && repeatValid && policyChecked && !loading
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun onPolicyCheckedChange(checked: Boolean) {
        _isPolicyChecked.value = checked
    }


    fun registerToServer(onNavigateToVerification: (email: String, token: String) -> Unit) {
        val isEmailValid = emailInput.validate()
        val isPasswordValid = passwordInput.validate()

        // Принудительно форсим проверку второго поля на совпадение перед отправкой запроса
        val isRepeatValid = passwordRepeatInput.validate()

        // Если строки физически не равны, прерываем выполнение еще до отправки на сервер
        if (!isEmailValid || !isPasswordValid || !isRepeatValid || passwordInput.text.value != passwordRepeatInput.text.value || !_isPolicyChecked.value) return

        viewModelScope.launch {
            _isLoading.value = true
            val cleanEmail = emailInput.text.value.trim()

            when (val result = authRepository.register(cleanEmail, passwordInput.text.value)) {
                is DomainResult.Success -> {
                    val responseBody = result.value
                    onNavigateToVerification(cleanEmail, responseBody.registrationSessionId)
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
            AuthErrorCode.AUTH_EMPTY_PASSWORD,
            AuthErrorCode.AUTH_PASSWORD_INVALID_LENGTH,
            AuthErrorCode.AUTH_PASSWORD_NO_DIGIT,
            AuthErrorCode.AUTH_PASSWORD_NO_UPPERCASE,
            AuthErrorCode.AUTH_PASSWORD_NO_LOWERCASE -> {
                passwordInput.setErrorFromBackend(code)
            }
            else -> {
                notificationManager.showNotification(originalError)
            }
        }
    }

}