package com.wiwchat.features.auth.presentation.viewmodel

import com.wiwchat.app.R
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wiwchat.features.auth.navigation.AuthDestinations
import com.wiwchat.shared.common.data.errors.AuthErrorCode
import com.wiwchat.shared.common.data.network.AppError
import com.wiwchat.shared.common.data.network.DomainResult
import com.wiwchat.shared.features.auth.domain.repository.AuthRepository
import com.wiwchat.shared.features.auth.domain.constants.AuthConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val args = savedStateHandle.toRoute<AuthDestinations.Verification>()

    val email: String = args.email

    private val _registrationSessionId = mutableStateOf(args.token ?: "")

    private val _otpCodeInput = mutableStateOf("")
    val otpCodeInput: State<String> = _otpCodeInput

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorStringResId = mutableStateOf<Int?>(null)
    val errorStringResId: State<Int?> = _errorStringResId

    private val _secondsRemaining = mutableIntStateOf(AuthConstants.INITIAL_TIMER_SECONDS)
    val secondsRemaining: State<Int> = _secondsRemaining

    private var timerJob: Job? = null

    init {
        startTimer()
    }

    fun onOtpCodeChanged(updatedCode: String) {
        // Ограничиваем ввод: только цифры и не длиннее 6 символов
        if (updatedCode.length <= AuthConstants.CODE_LENGTH && updatedCode.all { it.isDigit() }) {
            _otpCodeInput.value = updatedCode
            if (_errorStringResId.value != null) _errorStringResId.value = null
        }
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

    fun clearError() {
        if (_errorStringResId.value != null) {
            _errorStringResId.value = null
        }
    }

    // Метод отправки кода на сервер
    fun verifyCodeToServer() {
        if (_otpCodeInput.value.length < 6) {
            _errorStringResId.value = R.string.err_auth_invalid_code
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorStringResId.value = null

            when (val result = authRepository.verifyEmail(
                email = email,
                code = _otpCodeInput.value,
                registrationSessionId = _registrationSessionId.value
            )){
                is DomainResult.Success -> {
                    val responseBody = result.value
                    authRepository.saveSession(responseBody, email)
                }
                is DomainResult.Failure -> {
                    _errorStringResId.value = when (val error = result.error){
                        is AppError.NetworkUnavailable -> R.string.err_network_timeout
                        is AppError.Timeout -> R.string.err_network_timeout
                        is AppError.ServerMaintenance -> R.string.err_server_maintenance
                        is AppError.Unknown -> R.string.err_generic_unknown
                        is AppError.Backend -> {
                            when (error.code){
                                AuthErrorCode.OTP_INVALID_CODE -> R.string.err_auth_invalid_code
                                // токены
                                AuthErrorCode.OTP_EXPIRED_TOKEN,
                                AuthErrorCode.OTP_INVALID_TOKEN,
                                AuthErrorCode.OTP_EMPTY_TOKEN -> R.string.err_auth_code_expired
                                else -> R.string.err_generic_unknown
                            }
                        }
                    }
                }
            }

            _isLoading.value = false
        }

    }

    fun resendCodeToServer() {
        // Безопасность: не даем сделать повторный запрос, пока идет загрузка или таймер еще не дошел до 0
        if (_isLoading.value || _secondsRemaining.intValue > 0) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorStringResId.value = null

            when (val result = authRepository.resendCode(
                email = email,
                registrationSessionId = _registrationSessionId.value
            )) {
                is DomainResult.Success -> {
                    // Обновляем наш токен, так как сервер сгенерировал новый JWT с новым OTP внутри
                    _registrationSessionId.value = result.value.registrationSessionId
                    _otpCodeInput.value = "" // Очищаем старый ввод пользователя

                    // Перезапускаем таймер заново на 120 секунд
                    startTimer()
                }
                is DomainResult.Failure -> {
                    _errorStringResId.value = when (val error = result.error) {
                        is AppError.NetworkUnavailable -> R.string.err_network_timeout
                        is AppError.Timeout -> R.string.err_network_timeout
                        is AppError.ServerMaintenance -> R.string.err_server_maintenance
                        is AppError.Unknown -> R.string.err_generic_unknown
                        is AppError.Backend -> {
                            when (error.code) {
                                AuthErrorCode.OTP_INVALID_TOKEN -> R.string.err_auth_code_expired
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