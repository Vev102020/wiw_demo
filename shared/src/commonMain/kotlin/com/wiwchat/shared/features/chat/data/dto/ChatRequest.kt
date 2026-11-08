package com.wiwchat.shared.features.chat.data.dto

import com.wiwchat.shared.core.message.domain.model.MessageContent
import kotlinx.serialization.Serializable


@Serializable
data class CreateMessageRequest(
    val chatId: String,
    val content: MessageContent
)
