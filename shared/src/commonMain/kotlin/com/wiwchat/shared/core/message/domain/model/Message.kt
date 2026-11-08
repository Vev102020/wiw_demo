package com.wiwchat.shared.core.message.domain.model

import com.wiwchat.shared.core.message.data.dto.MessageStatus
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: MessageContent,
    val sentAt: Long,
    val editedAt: Long? = null,
    val status: MessageStatus = MessageStatus.SENT,
    val isDeleted: Boolean = false
)



