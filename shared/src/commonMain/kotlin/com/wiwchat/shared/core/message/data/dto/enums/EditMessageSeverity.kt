package com.wiwchat.shared.core.message.data.dto.enums

enum class EditMessageSeverity {
    TEXT_MINOR, // незначительное
    TEXT_MAJOR, // значительное текст
    MEDIA_MAJOR, // медиа значительное
    CRITICAL_SWAP // критическое: текст и медиа
}