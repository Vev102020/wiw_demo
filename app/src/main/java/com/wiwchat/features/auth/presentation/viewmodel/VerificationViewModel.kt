package com.wiwchat.features.auth.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wiwchat.features.auth.navigation.AuthDestinations
import com.wiwchat.shared.common.domain.errors.AuthErrorCode
import com.wiwchat.shared.common.domain.model.AppError
import com.wiwchat.shared.core.notification.NotificationManager
import com.wiwchat.shared.features.auth.data.dto.enums.VerificationType
import com.wiwchat.shared.features.auth.domain.constants.AuthConstants
import com.wiwchat.shared.features.auth.domain.usecase.ResendOtpResult
import com.wiwchat.shared.features.auth.domain.usecase.ResendOtpUseCase
import com.wiwchat.shared.features.auth.domain.usecase.VerifyOtpResult
import com.wiwchat.shared.features.auth.domain.usecase.VerifyOtpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val resendOtpUseCase: ResendOtpUseCase,
    private val notificationManager: NotificationManager,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val args = savedStateHandle.toRoute<AuthDestinations.Verification>()

    val email: String = args.email
    private val verifyType: VerificationType = args.verifyType
    private val _sessionToken = MutableStateFlow(args.token ?: "")

    private val _otpCodeInput = MutableStateFlow("")
    val otpCodeInput: StateFlow<String> = _otpCodeInput.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _secondsRemaining = MutableStateFlow(AuthConstants.INITIAL_TIMER_SECONDS)
    val secondsRemaining: StateFlow<Int> = _secondsRemaining.asStateFlow()

    private var timerJob: Job? = null

    private val _otpError = MutableStateFlow<AuthErrorCode?>(null)
    val otpError: StateFlow<AuthErrorCode?> = _otpError.asStateFlow()

    val isButtonEnabled: StateFlow<Boolean> = combine(
        _otpCodeInput,
        _isLoading
    ) { code, loading ->
        code.length == AuthConstants.CODE_LENGTH && !loading
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        // Если пришли со страницы логина (токен пустой), разрешаем сразу запросить новый код
        if (verifyType == VerificationType.REGISTRATION && _sessionToken.value.isBlank()) {
            _secondsRemaining.value = 0
        } else {
            startTimer()
        }
    }

    fun onOtpCodeChanged(updatedCode: String) {
        if (updatedCode.length <= AuthConstants.CODE_LENGTH && updatedCode.all { it.isDigit() }) {
            _otpCodeInput.value = updatedCode
            _otpError.value = null
        }
    }

    fun startTimer(){
        timerJob?.cancel()
        _secondsRemaining.value = AuthConstants.INITIAL_TIMER_SECONDS

        timerJob = viewModelScope.launch {
            while (_secondsRemaining.value > 0){
                delay(1000L.milliseconds)
                _secondsRemaining.value -= 1
            }
        }
    }

    // Метод отправки кода на сервер
    fun verifyOtpToServer(
        onNavigateToResetPassword: (email: String, verifiedResetToken: String) -> Unit
    ) {
        if (_otpCodeInput.value.length != AuthConstants.CODE_LENGTH) return

        viewModelScope.launch {
            _isLoading.value = true

            when (val result = verifyOtpUseCase(
                email = email,
                code = _otpCodeInput.value,
                sessionToken = _sessionToken.value,
                verifyType = verifyType
            )){
                is VerifyOtpResult.RegistrationSuccess -> {
                    timerJob?.cancel()
                }
                is VerifyOtpResult.ResetCodeVerified -> {
                    timerJob?.cancel()
                    onNavigateToResetPassword(email, result.verifiedResetToken)
                }
                is VerifyOtpResult.Error -> {
                    handleFailure(result.error)
                }
            }

            _isLoading.value = false
        }

    }

    fun resendOtpToServer() {
        // Не даем сделать повторный запрос
        if (
            _isLoading.value ||
            _secondsRemaining.value > 0
        ) return

        viewModelScope.launch {
            _isLoading.value = true

            when (val result = resendOtpUseCase(
                email = email,
                currentSessionToken = _sessionToken.value,
                verifyType = verifyType
            )) {
                is ResendOtpResult.Success -> {
                    _sessionToken.value = result.newSessionToken
                    _otpCodeInput.value = ""
                    _otpError.value = null
                    startTimer()
                }
                is ResendOtpResult.Error -> {
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
            // Неверный код
            AuthErrorCode.RESET_INVALID_CODE,
            AuthErrorCode.OTP_INVALID_CODE -> {
                _otpError.value = code
            }

            else -> {
                notificationManager.showNotification(originalError)
            }
        }
    }


}