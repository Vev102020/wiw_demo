package com.wiwchat.features.chat.data.mapper

import com.wiwchat.features.chat.presentation.model.*
import com.wiwchat.shared.features.chat.data.dto.enums.ChatType
import com.wiwchat.shared.features.chat.domain.model.*

fun Chat.toUi(currentUserId: String?): ChatUiModel {
    val displayName = name ?: participants.take(2).map { it.substringBefore('@') }.joinToString(", ")
    val avatarText = name?.firstOrNull()?.uppercase()
        ?: participants.firstOrNull()?.substringBefore('@')?.firstOrNull()?.uppercase()
        ?: "?"

    return ChatUiModel(
        id = id,
        type = when (type) {
            ChatType.DIRECT -> ChatUiModelType.Private
            ChatType.GROUP -> ChatUiModelType.Group
            ChatType.SAVED -> ChatUiModelType.Saved
        },
        name = displayName,
        avatarText = avatarText,
        latestMessage = null,
        time = "14:30",
        unreadCount = unreadCount
    )
}