package com.wiwchat.features.auth.presentation.viewmodel

import com.wiwchat.app.R
import androidx.compose.runtime.*
import androidx.lifecycle.*
import androidx.navigation.toRoute
import com.wiwchat.features.auth.navigation.AuthDestinations
import com.wiwchat.features.auth.presentation.state.PasswordFieldState
import com.wiwchat.shared.common.data.errors.AuthErrorCode
import com.wiwchat.shared.common.data.network.AppError
import com.wiwchat.shared.common.data.network.DomainResult
import com.wiwchat.shared.features.auth.domain.repository.AuthRepository
import com.wiwchat.shared.features.auth.domain.constants.AuthConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = savedStateHandle.toRoute<AuthDestinations.ResetPassword>()

    val email: String = args.email
    private val resetToken: String = args.token


    private val _otpCodeInput = mutableStateOf("")
    val otpCodeInput: State<String> = _otpCodeInput

    val newPasswordInput =
        PasswordFieldState(isRegistrationMode = true) // Стрикт-валидация для нового пароля
    val newPasswordRepeatInput = PasswordFieldState(isRegistrationMode = false)

    // Стейты экрана
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _serverErrorResId = mutableStateOf<Int?>(null)
    val serverErrorResId: State<Int?> = _serverErrorResId

    // Таймер для повторной отправки кода
    private val _secondsRemaining = mutableIntStateOf(AuthConstants.INITIAL_TIMER_SECONDS)
    val secondsRemaining: State<Int> = _secondsRemaining

    private var timerJob: Job? = null
    private var wasPasswordRepeatFocused = false

    init {
        startTimer()
    }

    // Логика ввода OTP (только цифры и не длиннее 6)
    fun onOtpCodeChanged(updatedCode: String) {
        if (updatedCode.length <= AuthConstants.CODE_LENGTH && updatedCode.all { it.isDigit() }) {
            _otpCodeInput.value = updatedCode
            if (_serverErrorResId.value != null) _serverErrorResId.value = null
        }
    }

    // Управление фокусом повтора пароля
    fun onPasswordRepeatFocusChanged(isFocused: Boolean) {
        if (isFocused) {
            wasPasswordRepeatFocused = true
        } else if (wasPasswordRepeatFocused) {
            validatePasswordMatch()
        }
    }

    private fun validatePasswordMatch(): Boolean {
        val matches = newPasswordInput.text.value == newPasswordRepeatInput.text.value
        newPasswordRepeatInput.errorResId.value = if (!matches) {
            R.string.err_auth_passwords_dont_match
        } else {
            null
        }
        return matches
    }

    fun startTimer() {
        timerJob?.cancel()
        _secondsRemaining.intValue = AuthConstants.INITIAL_TIMER_SECONDS

        timerJob = viewModelScope.launch {
            while (_secondsRemaining.intValue > 0) {
                delay(1000L.milliseconds)
                _secondsRemaining.intValue -= 1
            }
        }
    }

    // Активность кнопки сброса
    val isButtonEnabled: Boolean
        get() = _otpCodeInput.value.length == AuthConstants.CODE_LENGTH &&
                newPasswordInput.text.value.isNotBlank() &&
                newPasswordRepeatInput.text.value.isNotBlank() &&
                newPasswordInput.errorResId.value == null &&
                newPasswordRepeatInput.errorResId.value == null &&
                !_isLoading.value

    // Финальный метод отправки данных на сервер
    fun resetPasswordToServer(onNavigateToLogin: () -> Unit) {
        val isPasswordValid = newPasswordInput.validate(showEmptyError = true)
        val isRepeatValid = validatePasswordMatch()
        if (_otpCodeInput.value.length < AuthConstants.CODE_LENGTH || !isPasswordValid || !isRepeatValid) return

        viewModelScope.launch {
            _isLoading.value = true
            _serverErrorResId.value = null

            // Вызываем метод репозитория, который мы поправили (возвращает DomainResult<Unit>)
            when (val result = authRepository.resetPassword(
                email = email,
                code = _otpCodeInput.value,
                resetToken = resetToken,
                newPassword = newPasswordInput.text.value
            )) {
                is DomainResult.Success -> {
                    // Успешно сбросили пароль и аннулировали сессии — перекидываем на Login
                    onNavigateToLogin()
                }
                is DomainResult.Failure -> {
                    _serverErrorResId.value = when (val error = result.error) {
                        is AppError.NetworkUnavailable -> R.string.err_network_timeout
                        is AppError.Timeout -> R.string.err_network_timeout
                        is AppError.ServerMaintenance -> R.string.err_server_maintenance
                        is AppError.Unknown -> R.string.err_generic_unknown
                        is AppError.Backend -> {
                            when (error.code) {
                                AuthErrorCode.RESET_INVALID_CODE -> R.string.err_auth_invalid_code
                                AuthErrorCode.RESET_TOKEN_EXPIRED,
                                AuthErrorCode.RESET_TOKEN_INVALID -> R.string.err_auth_code_expired

                                // Ошибки сложной валидации нового пароля
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