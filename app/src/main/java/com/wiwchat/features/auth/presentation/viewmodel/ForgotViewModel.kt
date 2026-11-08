package com.wiwchat.features.auth.presentation.viewmodel

import com.wiwchat.app.R
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiwchat.features.auth.presentation.state.EmailFieldState
import com.wiwchat.shared.common.data.errors.AuthErrorCode
import com.wiwchat.shared.common.data.network.AppError
import com.wiwchat.shared.common.data.network.DomainResult
import com.wiwchat.shared.features.auth.domain.repository.AuthRepository
import jakarta.inject.Inject
import kotlinx.coroutines.launch

class ForgotViewModel @Inject constructor(
    private val authRepository : AuthRepository
) : ViewModel() {
    val emailInput = EmailFieldState()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _serverErrorResId = mutableStateOf<Int?>(null)
    val serverErrorResId: State<Int?> = _serverErrorResId

    // Кнопка активна, если поле заполнено, нет локальных ошибок и не идет загрузка
    val isButtonEnabled: Boolean
        get() = emailInput.text.value.isNotBlank() &&
                emailInput.errorResId.value == null &&
                !_isLoading.value

    fun sendResetCode(onNavigateToResetVerification: (email: String, resetToken: String) -> Unit) {
        val isEmailValid = emailInput.validate(showEmptyError = true)
        if (!isEmailValid) return

        viewModelScope.launch {
            _isLoading.value = true
            _serverErrorResId.value = null

            when (val result = authRepository.forgotPassword(emailInput.text.value)) {
                is DomainResult.Success -> {
                    val responseBody = result.value
                    // Переходим на экран ввода OTP и нового пароля
                    onNavigateToResetVerification(emailInput.text.value, responseBody.registrationSessionId)
                }

                is DomainResult.Failure -> {
                    _serverErrorResId.value = when (val error = result.error) {
                        is AppError.NetworkUnavailable -> R.string.err_network_timeout
                        is AppError.Timeout -> R.string.err_network_timeout
                        is AppError.ServerMaintenance -> R.string.err_server_maintenance
                        is AppError.Unknown -> R.string.err_generic_unknown
                        is AppError.Backend -> {
                            when (error.code) {
                                AuthErrorCode.AUTH_INVALID_EMAIL_FORMAT -> R.string.err_auth_invalid_email_format
                                AuthErrorCode.AUTH_EMPTY_EMAIL -> R.string.err_auth_empty_email
                                AuthErrorCode.AUTH_TOO_MANY_ATTEMPTS -> R.string.err_auth_too_many_attempts
                                else -> R.string.err_generic_unknown
                            }
                        }
                    }
                }
            }
            _isLoading.value = false
        }
    }

}