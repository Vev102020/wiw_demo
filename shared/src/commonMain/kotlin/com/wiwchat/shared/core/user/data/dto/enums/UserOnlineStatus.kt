package com.wiwchat.shared.core.user.data.dto.enums

enum class UserOnlineStatus {
    ONLINE,      // сейчас в приложении
    OFFLINE,     // вышел
    AWAY,        // неактивен 5+ минут
    DO_NOT_DISTURB  // не беспокоить
}