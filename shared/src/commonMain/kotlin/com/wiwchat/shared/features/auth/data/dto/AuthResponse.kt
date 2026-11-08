package com.wiwchat.shared.features.auth.data.dto

import com.wiwchat.shared.features.profile.data.dto.UserProfileDto
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer",
    val userId: String
)

@Serializable
data class LoginResponse(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val needsVerification: Boolean,
    val role: String,
    val userProfile: UserProfileDto
)

@Serializable
data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

@Serializable
data class RegisterResponse(
    val registrationSessionId: String,
    val status: String
)

@Serializable
data class StatusResponse(
    val status: String = "success",
    val message: String? = null
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String
)