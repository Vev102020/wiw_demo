package com.wiwchat.shared.core.user.data.dto

import com.wiwchat.shared.core.user.data.dto.enums.UserOnlineStatus
import kotlinx.serialization.Serializable

@Serializable
data class UserStatusEvent(
    val userId: String,
    val status: UserOnlineStatus,
    val lastSeen: Long
)