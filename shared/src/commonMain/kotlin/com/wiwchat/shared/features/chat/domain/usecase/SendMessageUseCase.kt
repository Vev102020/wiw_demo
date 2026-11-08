package com.wiwchat.shared.features.chat.domain.usecase

import com.wiwchat.shared.features.chat.domain.repository.ChatRepository
import jakarta.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(chatId: String, text: String) = repository.sendMessage(chatId, text)
}