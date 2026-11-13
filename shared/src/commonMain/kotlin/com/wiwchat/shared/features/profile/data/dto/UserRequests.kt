package com.wiwchat.shared.features.profile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateDisplayNameRequest(
    val displayName: String
)

@Serializable
data class UpdateUsernameRequest(
    val username: String
)