package com.wiwchat.features.auth.presentation.state

import com.wiwchat.app.R
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.wiwchat.shared.common.data.errors.AuthErrorCode
import com.wiwchat.shared.features.auth.domain.constants.AuthConstants
import com.wiwchat.shared.features.auth.domain.validator.*


interface InputFieldState {
    val text: State<String>
    val errorResId: State<Int?>
    fun onTextChanged(value: String)
    fun onFocusChanged(isFocused: Boolean)
    fun validate(showEmptyError: Boolean): Boolean
}

class EmailFieldState : InputFieldState {
    override val text = mutableStateOf("")
    override val errorResId = mutableStateOf<Int?>(null)
    private var wasFocused = false

    override fun onTextChanged(value: String) {
        text.value = if (value.length <= AuthConstants.EMAIL_MAX_LENGTH) value else value.take(
            AuthConstants.EMAIL_MAX_LENGTH)

        validate(showEmptyError = false)
    }

    override fun onFocusChanged(isFocused: Boolean) {
        if (isFocused) {
            wasFocused = true
        } else if (wasFocused) {
            // Строгая проверка при потере фокуса
            validate(showEmptyError = true)
        }
    }

    override fun validate(showEmptyError: Boolean): Boolean {
        val error = AuthValidator.validateEmail(text.value)
        errorResId.value = when (error) {
            AuthErrorCode.AUTH_EMPTY_EMAIL -> if (showEmptyError) R.string.err_auth_empty_email else null
            AuthErrorCode.AUTH_INVALID_EMAIL_FORMAT -> R.string.err_auth_invalid_email_format
            else -> null
        }
        return error == null
    }
}

class PasswordFieldState(
    private val isRegistrationMode: Boolean = false
) : InputFieldState {
    override val text = mutableStateOf("")
    override val errorResId = mutableStateOf<Int?>(null)
    private var wasFocused = false

    override fun onTextChanged(value: String) {
        text.value = if (value.length <= AuthConstants.PASSWORD_MAX_LENGTH) value else value.take(
            AuthConstants.PASSWORD_MAX_LENGTH)
        validate(showEmptyError = false)
    }

    override fun onFocusChanged(isFocused: Boolean) {
        if (isFocused) {
            wasFocused = true
        } else if (wasFocused) {
            validate(showEmptyError = true)
        }
    }

    override fun validate(showEmptyError: Boolean): Boolean {
        if (!isRegistrationMode) {
            errorResId.value = if (text.value.isBlank() && showEmptyError) R.string.err_auth_empty_password else null
            return text.value.isNotBlank()
        }

        val error = AuthValidator.validatePassword(text.value)
        errorResId.value = when (error) {
            AuthErrorCode.AUTH_EMPTY_PASSWORD -> if (showEmptyError) R.string.err_auth_empty_password else null
            AuthErrorCode.AUTH_PASSWORD_INVALID_LENGTH -> R.string.err_auth_pass_length
            AuthErrorCode.AUTH_PASSWORD_NO_DIGIT -> R.string.err_auth_pass_digit
            AuthErrorCode.AUTH_PASSWORD_NO_UPPERCASE -> R.string.err_auth_pass_upper
            AuthErrorCode.AUTH_PASSWORD_NO_LOWERCASE -> R.string.err_auth_pass_lower
            else -> null
        }
        return error == null
    }
}
