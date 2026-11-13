package com.wiwchat.shared.features.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
)

@Serializable
data class VerifyOtpRequest(
    val email: String,
    val code: String,
    val registrationSessionId: String
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class VerifyResetOtpRequest(
    val email: String,
    val code: String,
    val resetToken: String,
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val resetToken: String,
    val newPassword: String
)

@Serializable
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

@Serializable
data class ResendOtpRequest(
    val email: String,
    val registrationSessionId: String
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class LogoutRequest(
    val refreshToken: String
)

