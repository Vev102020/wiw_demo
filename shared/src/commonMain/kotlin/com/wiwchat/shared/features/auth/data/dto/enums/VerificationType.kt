package com.wiwchat.shared.features.auth.data.dto.enums

enum class VerificationType {
    REGISTRATION,   // Подтверждение после регистрации -> сохраняем сессию и пускаем в чат
    RESET_PASSWORD  // Подтверждение при сбросе -> переходим на экран ввода нового пароля
}