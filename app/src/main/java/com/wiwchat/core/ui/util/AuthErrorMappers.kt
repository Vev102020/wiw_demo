package com.wiwchat.core.ui.util

import com.wiwchat.app.R
import com.wiwchat.shared.common.domain.errors.AuthErrorCode
import com.wiwchat.shared.common.domain.model.AppError

fun mapAuthErrorToId(error: AuthErrorCode): Int {
    return when (error) {
        AuthErrorCode.AUTH_EMPTY_EMAIL -> R.string.err_auth_empty_email
        AuthErrorCode.AUTH_INVALID_EMAIL_FORMAT -> R.string.err_auth_invalid_email_format
        AuthErrorCode.AUTH_EMPTY_PASSWORD -> R.string.err_auth_empty_password
        AuthErrorCode.AUTH_PASSWORD_INVALID_LENGTH -> R.string.err_auth_pass_length
        AuthErrorCode.AUTH_PASSWORD_NO_DIGIT -> R.string.err_auth_pass_digit
        AuthErrorCode.AUTH_PASSWORD_NO_UPPERCASE -> R.string.err_auth_pass_upper
        AuthErrorCode.AUTH_PASSWORD_NO_LOWERCASE -> R.string.err_auth_pass_lower
        AuthErrorCode.AUTH_INVALID_CREDENTIALS -> R.string.err_auth_invalid_credentials
        AuthErrorCode.AUTH_OLD_PASSWORD_INVALID -> R.string.err_auth_old_password_invalid
        else -> R.string.err_generic_unknown
    }
}

fun mapAppErrorToId(error: AppError): Int {
    return when (error) {
        is AppError.NetworkUnavailable -> R.string.err_network_timeout
        is AppError.ServerUnreachable -> R.string.err_server_unreachable
        is AppError.Timeout -> R.string.err_generic_unknown
        is AppError.ServerMaintenance -> R.string.err_server_maintenance
        is AppError.Backend -> {
            if (error.code == AuthErrorCode.AUTH_TOO_MANY_ATTEMPTS) {
                R.string.err_auth_too_many_attempts
            } else {
                R.string.err_generic_unknown
            }
        }
        else -> R.string.err_generic_unknown
    }
}