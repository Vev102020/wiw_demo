package com.wiwchat.shared.features.profile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserSearchDto(
    val id: String,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?
)
