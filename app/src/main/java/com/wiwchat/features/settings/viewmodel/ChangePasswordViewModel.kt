package com.wiwchat.features.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiwchat.shared.common.domain.model.AppError
import com.wiwchat.shared.common.domain.model.DomainResult
import com.wiwchat.shared.common.data.network.WebSocketManager
import com.wiwchat.shared.common.domain.errors.AuthErrorCode
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
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val webSocketManager: WebSocketManager,
    private val notificationManager: NotificationManager
) : ViewModel() {

    val oldPasswordInput = FormFieldState(
        scope = viewModelScope,
        maxLength = AuthConstants.PASSWORD_MAX_LENGTH,
        validator = { text ->
            if (text.isBlank()) AuthErrorCode.AUTH_EMPTY_PASSWORD else null
        }
    )

    val newPasswordInput = FormFieldState(
        scope = viewModelScope,
        maxLength = AuthConstants.PASSWORD_MAX_LENGTH,
        validator = { text -> AuthValidator.validatePassword(text) }
    )

    val newPasswordRepeatInput = FormFieldState(
        scope = viewModelScope,
        maxLength = AuthConstants.PASSWORD_MAX_LENGTH,
        validator = { text ->
            if (text != newPasswordInput.text.value) {
                AuthErrorCode.AUTH_PASSWORD_INVALID_LENGTH
            } else null
        }
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val isButtonEnabled: StateFlow<Boolean> = combine(
        oldPasswordInput.isValid,
        newPasswordInput.isValid,
        newPasswordRepeatInput.isValid,
        _isLoading
    ) { oldValid, newValid, repeatValid, loading ->
        oldValid && newValid && repeatValid && !loading
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun changePasswordToServer(onNavigateBack: () -> Unit) {
        val isOldValid = oldPasswordInput.validate()
        val isNewValid = newPasswordInput.validate()
        val isRepeatValid = newPasswordRepeatInput.validate()

        if (!isOldValid || !isNewValid || !isRepeatValid || newPasswordInput.text.value != newPasswordRepeatInput.text.value) return

        viewModelScope.launch {
            _isLoading.value = true

            when (val result = authRepository.changePassword(
                oldPassword = oldPasswordInput.text.value,
                newPassword = newPasswordInput.text.value
            )) {
                is DomainResult.Success -> {
                    val responseBody = result.value

                    webSocketManager.reconnect(responseBody.userId)
                    onNavigateBack()
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
            // Если старый пароль введен неверно — подсвечиваем первое поле
            AuthErrorCode.AUTH_OLD_PASSWORD_INVALID -> {
                oldPasswordInput.setErrorFromBackend(code)
            }
            // Ошибки сложности нового пароля — подсвечиваем второе поле
            AuthErrorCode.AUTH_EMPTY_PASSWORD,
            AuthErrorCode.AUTH_PASSWORD_INVALID_LENGTH,
            AuthErrorCode.AUTH_PASSWORD_NO_DIGIT,
            AuthErrorCode.AUTH_PASSWORD_NO_UPPERCASE,
            AuthErrorCode.AUTH_PASSWORD_NO_LOWERCASE -> {
                newPasswordInput.setErrorFromBackend(code)
            }
            else -> {
                notificationManager.showNotification(originalError)
            }
        }
    }
}