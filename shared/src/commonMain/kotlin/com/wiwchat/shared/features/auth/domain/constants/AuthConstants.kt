package com.wiwchat.shared.features.auth.domain.constants

object AuthConstants {
    // Ограничения для Email
    const val EMAIL_MAX_LENGTH = 50
    val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()

    // Ограничения для Пароля
    const val PASSWORD_MIN_LENGTH = 8
    const val PASSWORD_MAX_LENGTH = 25

    const val CODE_LENGTH = 6

    const val INITIAL_TIMER_SECONDS = 120

    const val DEFAULT_DEVICE_INFO = "Unknown Device"
}