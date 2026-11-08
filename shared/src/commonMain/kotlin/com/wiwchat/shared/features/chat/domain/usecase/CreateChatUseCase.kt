package com.wiwchat.shared.features.chat.domain.usecase

import com.wiwchat.shared.features.chat.domain.repository.ChatRepository
import jakarta.inject.Inject

class CreateChatUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(targetUserId: String) = repository.createChat(targetUserId)
}