package com.wiwchat.shared.common.data.dto.enums

import kotlinx.serialization.Serializable

@Serializable
enum class WebSocketSystemAction {
    // Отправляется, когда пользователь сам нажал
    // «Выйти на всех устройствах» или сменил пароль (аккаунт живой, но токены сгорели).
    FORCE_LOGOUT,

    // Отправляется, когда администратор забанил пользователя.
    // «Ваш аккаунт заблокирован» и полностью заблокировать интерфейс.
    ACCOUNT_SUSPENDED
}