package com.wiwchat.shared.features.auth.domain.validator

import com.wiwchat.shared.common.domain.errors.AuthErrorCode
import com.wiwchat.shared.features.auth.domain.constants.AuthConstants


object AuthValidator {
    fun validateEmail(email: String): AuthErrorCode? {
        val clean = email.trim()
        return when {
            clean.isEmpty() -> AuthErrorCode.AUTH_EMPTY_EMAIL
            clean.length > AuthConstants.EMAIL_MAX_LENGTH -> AuthErrorCode.AUTH_INVALID_EMAIL_FORMAT
            !AuthConstants.EMAIL_REGEX.matches(clean) -> AuthErrorCode.AUTH_INVALID_EMAIL_FORMAT
            else -> null
        }
    }

    fun validatePassword(password: String): AuthErrorCode? {
        if (password.isEmpty()) {
            return AuthErrorCode.AUTH_EMPTY_PASSWORD
        }

        if (password.length !in AuthConstants.PASSWORD_MIN_LENGTH..AuthConstants.PASSWORD_MAX_LENGTH) {
            return AuthErrorCode.AUTH_PASSWORD_INVALID_LENGTH
        }

        if (!password.any { it.isDigit() }) {
            return AuthErrorCode.AUTH_PASSWORD_NO_DIGIT
        }

        if (!password.any { it.isUpperCase() }) {
            return AuthErrorCode.AUTH_PASSWORD_NO_UPPERCASE
        }

        if (!password.any { it.isLowerCase() }) {
            return AuthErrorCode.AUTH_PASSWORD_NO_LOWERCASE
        }

        return null
    }
}