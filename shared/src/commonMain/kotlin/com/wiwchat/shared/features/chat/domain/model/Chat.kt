package com.wiwchat.shared.features.chat.domain.model

import com.wiwchat.shared.features.chat.data.dto.enums.ChatType

data class Chat(
    val id: String,
    val type: ChatType,
    val name: String? = null,
    val participants: List<String>,
    val latestMessageId: String? = null,
    val unreadCount: Int = 0,
    val createdAt: Long
)

