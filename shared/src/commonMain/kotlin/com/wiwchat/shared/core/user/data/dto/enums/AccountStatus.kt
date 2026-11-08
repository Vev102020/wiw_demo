package com.wiwchat.shared.core.user.data.dto.enums

enum class AccountStatus {
    PENDING_VERIFICATION,   // только зарегистрировался, код не введён
    ACTIVE,                 // верифицирован, полный доступ
    SUSPENDED,              // заблокирован админом
    DELETED                 // удалён
}