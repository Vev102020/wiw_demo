package com.wiwchat.shared.core.user.data.dto.enums

enum class RelationType {
    MUTE_SOUND,       // Уровень 1: Только заглушить звук
    HIDE_MESSAGES,    // Уровень 2: Полностью скрывать сообщения в чатах
    GLOBAL_BLOCK      // Уровень 3: Черный список (полная блокировка)
}