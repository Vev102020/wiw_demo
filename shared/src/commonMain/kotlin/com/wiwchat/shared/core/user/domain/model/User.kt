package com.wiwchat.shared.core.user.domain.model

import com.wiwchat.shared.core.user.data.dto.enums.UserOnlineStatus

data class UserModel(
    val id: String,
    val username: String,
    val avatarUrl: String? = null,
    val status: UserOnlineStatus = UserOnlineStatus.ONLINE,
    val lastSeenAt: Long? = null
)

