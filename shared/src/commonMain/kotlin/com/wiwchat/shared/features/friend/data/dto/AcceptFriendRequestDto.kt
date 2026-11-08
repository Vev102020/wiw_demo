package com.wiwchat.shared.features.friend.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AcceptFriendRequestDto(
    val requestId: String
)