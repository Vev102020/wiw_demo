package com.wiwchat.shared.core.message.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class MessageContent {
    data class Text(val text: String) : MessageContent()
    data class Voice(
        val fileUrl: String,      // ссылка на аудио
        val durationMs: Int,     // длительность в мс
        val mimeType: String = "audio/mp4"
    ) : MessageContent()
}