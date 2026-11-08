package com.wiwchat.features.chat.presentation.model

data class MessageUiModel(
    val id: String,
    val text: String,
    val isSelf: Boolean,
    val time: String,
    val senderName: String?,
    val senderAvatar: String?,
    val status: MessageStatus
)

sealed interface MessageStatus {
    object Sending : MessageStatus
    object Sent : MessageStatus
    object Read : MessageStatus
    object Error : MessageStatus
}