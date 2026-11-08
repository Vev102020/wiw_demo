package com.wiwchat.features.chat.presentation.model

data class ChatUiModel(
    val id: String,
    val type: ChatUiModelType,
    val name: String,
    val avatarText: String,
    val latestMessage: String?,
    val time: String,
    val unreadCount: Int
)

sealed class ChatUiModelType  {
    object Private : ChatUiModelType();
    object Group : ChatUiModelType()
    object Saved : ChatUiModelType()
}

