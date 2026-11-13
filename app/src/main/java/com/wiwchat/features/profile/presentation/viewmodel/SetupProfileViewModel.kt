package com.wiwchat.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiwchat.shared.common.domain.errors.ProfileErrorCode
import com.wiwchat.shared.common.domain.model.AppError
import com.wiwchat.shared.common.domain.model.DomainResult
import com.wiwchat.shared.core.forms.FormFieldState
import com.wiwchat.shared.core.notification.NotificationManager
import com.wiwchat.shared.features.profile.domain.constants.ProfileConstants
import com.wiwchat.shared.features.profile.domain.repository.ProfileRepository
import com.wiwchat.shared.features.profile.domain.validator.ProfileValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SetupProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val notificationManager: NotificationManager
) : ViewModel() {

    val displayNameInput = FormFieldState(
        scope = viewModelScope,
        maxLength = ProfileConstants.DISPLAY_NAME_MAX_LENGTH,
        validator = { text -> ProfileValidator.validateDisplayName(text) }
    )

    val usernameInput = FormFieldState(
        scope = viewModelScope,
        maxLength = ProfileConstants.USERNAME_MAX_LENGTH,
        validator = { text -> ProfileValidator.validateUsername(text) }
    )

    private var initialUsername: String = ""

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val isButtonEnabled: StateFlow<Boolean> = combine(
        displayNameInput.isValid,
        usernameInput.isValid,
        _isLoading
    ) { nameValid, usernameValid, loading ->
        nameValid && usernameValid && !loading
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        viewModelScope.launch {
            val user = profileRepository.observeCurrentUser().filterNotNull().first()
            val generatedUsername = user.username.orEmpty()
            initialUsername = generatedUsername
            if (usernameInput.text.value.isEmpty() && generatedUsername.isNotEmpty()) {
                usernameInput.onTextChanged(generatedUsername)
            }
        }
    }

    fun saveProfileSetup() {
        val isNameValid = displayNameInput.validate()
        val isUsernameValid = usernameInput.validate()
        if (!isNameValid || !isUsernameValid) return

        viewModelScope.launch {
            _isLoading.value = true

            val cleanDisplayName = displayNameInput.text.value.trim()
            val cleanUsername = usernameInput.text.value.trim().lowercase()

            // Если пользователь изменил сгенерированный username, сначала обновляем его
            if (cleanUsername != initialUsername) {
                when (val usernameResult = profileRepository.updateUsername(cleanUsername)) {
                    is DomainResult.Failure -> {
                        handleFailure(usernameResult.error)
                        _isLoading.value = false
                        return@launch
                    }
                    is DomainResult.Success -> {
                        initialUsername = cleanUsername
                    }
                }
            }

            // Обновляем displayName (после этого Room обновится и AppNavGraph сам откроет MainGraph)
            when (val nameResult = profileRepository.updateDisplayName(cleanDisplayName)) {
                is DomainResult.Failure -> {
                    handleFailure(nameResult.error)
                }
                is DomainResult.Success -> Unit
            }

            _isLoading.value = false
        }
    }

    private fun handleFailure(error: AppError) {
        if (error is AppError.Backend) {
            val profileCode = error.code as? ProfileErrorCode
            if (profileCode != null) {
                handleBackendError(profileCode, error)
            } else {
                notificationManager.showNotification(error)
            }
        } else {
            notificationManager.showNotification(error)
        }
    }

    private fun handleBackendError(code: ProfileErrorCode, originalError: AppError.Backend) {
        when (code) {
            ProfileErrorCode.PROFILE_EMPTY_DISPLAY_NAME,
            ProfileErrorCode.PROFILE_DISPLAY_NAME_INVALID_LENGTH -> {
                displayNameInput.setErrorFromBackend(code)
            }
            ProfileErrorCode.PROFILE_EMPTY_USERNAME,
            ProfileErrorCode.PROFILE_USERNAME_INVALID_LENGTH,
            ProfileErrorCode.PROFILE_USERNAME_INVALID_FORMAT,
            ProfileErrorCode.PROFILE_USERNAME_ALREADY_TAKEN -> {
                usernameInput.setErrorFromBackend(code)
            }
        }
    }
}