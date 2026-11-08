package com.wiwchat.shared.features.chat.data.dto

import com.wiwchat.shared.core.message.domain.model.Message
import kotlinx.serialization.Serializable




@Serializable
data class GetMessagesResponse(
    val messages: List<Message>,
    val hasMore: Boolean,
    val nextCursor: String?
)


@Serializable
data class UploadMediaResponse(
    val url: String,
    val fileName: String,
    val contentType: String
)