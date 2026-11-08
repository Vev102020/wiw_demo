package com.wiwchat.shared.core.message.domain.model

import com.wiwchat.shared.core.message.data.dto.enums.MessageType
import java.util.UUID

data class SavedMessage (
    val id: UUID,
    val messageText: String,
    val fileUrl: String?,
    val timestamp: Long,
    val originChatId: Long, //id откуда забрали
    val originChatName: String, //Имя чата/сервера для сортировки
    val originSenderName: String, // Кто автор
    val contentType: MessageType,
)