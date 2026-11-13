package com.wiwchat.features.auth.presentation.viewmodel

import androidx.lifecycle.*
import androidx.navigation.toRoute
import com.wiwchat.features.auth.navigation.AuthDestinations
import com.wiwchat.shared.common.domain.errors.AuthErrorCode
import com.wiwchat.shared.common.domain.model.AppError
import com.wiwchat.shared.common.domain.model.DomainResult
import com.wiwchat.shared.core.forms.FormFieldState
import com.wiwchat.shared.core.notification.NotificationManager
import com.wiwchat.shared.features.auth.domain.repository.AuthRepository
import com.wiwchat.shared.features.auth.domain.constants.AuthConstants
import com.wiwchat.shared.features.auth.domain.validator.AuthValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationManager: NotificationManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<AuthDestinations.ResetPassword>()

    val email: String = args.email
    private val resetToken: String = args.token

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
        newPasswordInput.isValid,
        newPasswordRepeatInput.isValid,
        _isLoading
    ) { passValid, repeatValid, loading ->
        passValid && repeatValid && !loading
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun resetPasswordToServer(onNavigateToLogin: () -> Unit) {
        if (
            !newPasswordInput.validate() ||
            !newPasswordRepeatInput.validate() ||
            newPasswordInput.text.value != newPasswordRepeatInput.text.value
            ) return

        viewModelScope.launch {
            _isLoading.value = true

            when (val result = authRepository.resetPassword(
                email = email,
                resetToken = resetToken,
                newPassword = newPasswordInput.text.value
            )) {
                is DomainResult.Success -> {
                    onNavigateToLogin()
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