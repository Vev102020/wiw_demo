package com.wiwchat.shared.common.domain.errors

enum class AuthErrorCode : ApiErrorCode {
    // Права
    AUTH_ACCOUNT_SUSPENDED,
    AUTH_INSUFFICIENT_PERMISSIONS,
    AUTH_INVALID_USER_ROLE,

    // Модуль Входа
    AUTH_INVALID_CREDENTIALS,
    AUTH_EMPTY_EMAIL,
    AUTH_INVALID_EMAIL_FORMAT,

    AUTH_EMPTY_PASSWORD,
    AUTH_OLD_PASSWORD_INVALID,
    AUTH_PASSWORD_INVALID_LENGTH,
    AUTH_PASSWORD_NO_DIGIT,
    AUTH_PASSWORD_NO_UPPERCASE,
    AUTH_PASSWORD_NO_LOWERCASE,

    AUTH_TOO_MANY_ATTEMPTS,

    // Модуль Верификации OTP
    OTP_EMPTY_CODE,
    OTP_INVALID_CODE,
    OTP_EMPTY_TOKEN,
    OTP_EXPIRED_TOKEN,
    OTP_INVALID_TOKEN,

    // Сброс пароля
    RESET_TOKEN_EXPIRED,
    RESET_TOKEN_INVALID,
    RESET_INVALID_CODE;

    companion object {
        fun find(name: String): ApiErrorCode? = entries.find { it.name == name }
    }
}