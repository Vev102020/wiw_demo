package com.wiwchat.shared.features.profile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    val id: String,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val status: String,
    val lastSeen: Long,
)


