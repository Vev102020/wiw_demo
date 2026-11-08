package com.wiwchat.shared.core.user.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String?,
    val role: String,
    val avatarUrl: String? = null,
)

