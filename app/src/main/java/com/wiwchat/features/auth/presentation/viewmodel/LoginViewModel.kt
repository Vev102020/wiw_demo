package com.wiwchat.features.auth.presentation.viewmodel

import com.wiwchat.app.R
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiwchat.features.auth.presentation.state.EmailFieldState
import com.wiwchat.features.auth.presentation.state.PasswordFieldState
import com.wiwchat.shared.common.data.errors.AuthErrorCode
import com.wiwchat.shared.common.data.network.AppError
import com.wiwchat.shared.common.data.network.DomainResult
import com.wiwchat.shared.features.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor (
    private val authRepository: AuthRepository
) : ViewModel() {

    val emailInput = EmailFieldState()
    val passwordInput = PasswordFieldState(isRegistrationMode = false)

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _serverErrorResId = mutableStateOf<Int?>(null)
    val serverErrorResId: State<Int?> = _serverErrorResId

    fun loginToServer(onNavigateToVerification: (email: String) -> Unit) {
        // Финальный форсированный чек перед отправкой (собираем булевы статусы)
        val isEmailValid = emailInput.validate(showEmptyError = true)
        val isPasswordValid = passwordInput.validate(showEmptyError = true)
        if (!isEmailValid || !isPasswordValid) return

        viewModelScope.launch {
            _isLoading.value = true
            _serverErrorResId.value = null

            when (val result = authRepository.login(
                emailInput.text.value,
                passwordInput.text.value)
            ) {
                is DomainResult.Success -> {
                    val responseBody = result.value
                    if (!responseBody.needsVerification) {
                        authRepository.saveSession(responseBody, emailInput.text.value)
                    } else {
                        onNavigateToVerification(emailInput.text.value)
                    }
                }
                is DomainResult.Failure -> {
                    // Мапим общую серверную ошибку (например, неверные учетные данные)
                    _serverErrorResId.value = when (val error = result.error) {
                        is AppError.NetworkUnavailable -> R.string.err_network_timeout
                        is AppError.Timeout -> R.string.err_network_timeout
                        is AppError.ServerMaintenance -> R.string.err_server_maintenance
                        is AppError.Unknown -> R.string.err_generic_unknown
                        is AppError.Backend -> {
                            when (error.code) {
                                AuthErrorCode.AUTH_INVALID_CREDENTIALS -> R.string.err_auth_invalid_credentials
                                else -> R.string.err_generic_unknown
                            }
                        }
                    }
                }
            }
            _isLoading.value = false
        }
    }

    val isButtonEnabled: Boolean
        get() = emailInput.text.value.isNotBlank() &&
                passwordInput.text.value.isNotBlank() &&
                emailInput.errorResId.value == null &&
                passwordInput.errorResId.value == null &&
                !_isLoading.value

}