package com.wiwchat.shared.features.chat.domain.repository

import com.wiwchat.shared.core.message.domain.model.Message
import com.wiwchat.shared.features.chat.domain.model.Chat

interface ChatRepository {
    suspend fun createChat(targetUserId: String): Result<String>
    suspend fun getChats(): Result<List<Chat>>
    suspend fun getMessages(chatId: String): Result<List<Message>>
    suspend fun sendMessage(chatId: String, text: String): Result<Message>
}