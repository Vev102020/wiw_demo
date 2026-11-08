package com.wiwchat.features.auth.presentation.viewmodel


import com.wiwchat.app.R
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle
import com.wiwchat.features.auth.presentation.state.EmailFieldState
import com.wiwchat.features.auth.presentation.state.PasswordFieldState
import com.wiwchat.shared.common.data.errors.AuthErrorCode
import com.wiwchat.shared.common.data.network.AppError
import com.wiwchat.shared.common.data.network.DomainResult
import com.wiwchat.shared.features.auth.domain.repository.AuthRepository


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(){

    val emailInput = EmailFieldState()
    val passwordInput = PasswordFieldState(isRegistrationMode = true)
    val passwordRepeatInput = PasswordFieldState(isRegistrationMode = false)

    private val _isPolicyChecked = mutableStateOf(false)
    val isPolicyChecked: State<Boolean> = _isPolicyChecked

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _serverErrorResId = mutableStateOf<Int?>(null)
    val serverErrorResId: State<Int?> = _serverErrorResId

    private var wasPasswordRepeatFocused = false

    fun onPasswordRepeatFocusChanged(isFocused: Boolean) {
        if (isFocused) {
            wasPasswordRepeatFocused = true
        } else if (wasPasswordRepeatFocused) {
            validatePasswordMatch()
        }
    }

    private fun validatePasswordMatch(): Boolean {
        val matches = passwordInput.text.value == passwordRepeatInput.text.value
        passwordRepeatInput.errorResId.value = if (!matches) {
            R.string.err_auth_passwords_dont_match
        } else {
            null
        }
        return matches
    }

    fun onPolicyCheckedChange(checked: Boolean) {
        _isPolicyChecked.value = checked
        if (_serverErrorResId.value != null) _serverErrorResId.value = null
    }

    val isButtonEnabled: Boolean
        get() = emailInput.text.value.isNotBlank() &&
                passwordInput.text.value.isNotBlank() &&
                passwordRepeatInput.text.value.isNotBlank() &&
                emailInput.errorResId.value == null &&
                passwordInput.errorResId.value == null &&
                passwordRepeatInput.errorResId.value == null &&
                _isPolicyChecked.value &&
                !_isLoading.value

    fun registerToServer(onNavigateToVerification: (email: String, token: String) -> Unit) {
        val isEmailValid = emailInput.validate(showEmptyError = true)
        val isPasswordValid = passwordInput.validate(showEmptyError = true)
        val isRepeatValid = validatePasswordMatch()

        if (!isEmailValid || !isPasswordValid || !isRepeatValid || !_isPolicyChecked.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _serverErrorResId.value = null

            when (val result = authRepository.register(
                emailInput.text.value.trim(),
                passwordInput.text.value
            )) {
                is DomainResult.Success -> {
                    val responseBody = result.value
                    onNavigateToVerification(
                        emailInput.text.value.trim(),
                        responseBody.registrationSessionId
                    )
                }
                is DomainResult.Failure -> {
                    _serverErrorResId.value = when (val error = result.error) {
                        is AppError.NetworkUnavailable -> R.string.err_network_timeout
                        is AppError.Timeout -> R.string.err_network_timeout
                        is AppError.ServerMaintenance -> R.string.err_server_maintenance
                        is AppError.Unknown -> R.string.err_generic_unknown
                        is AppError.Backend -> {
                            when (error.code) {
                                AuthErrorCode.AUTH_EMPTY_EMAIL -> R.string.err_auth_empty_email
                                AuthErrorCode.AUTH_EMPTY_PASSWORD -> R.string.err_auth_empty_password
                                AuthErrorCode.AUTH_INVALID_EMAIL_FORMAT -> R.string.err_auth_invalid_email_format

                                // Серверные ошибки сложной валидации пароля
                                AuthErrorCode.AUTH_PASSWORD_INVALID_LENGTH -> R.string.err_auth_pass_length
                                AuthErrorCode.AUTH_PASSWORD_NO_DIGIT -> R.string.err_auth_pass_digit
                                AuthErrorCode.AUTH_PASSWORD_NO_UPPERCASE -> R.string.err_auth_pass_upper
                                AuthErrorCode.AUTH_PASSWORD_NO_LOWERCASE -> R.string.err_auth_pass_lower

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