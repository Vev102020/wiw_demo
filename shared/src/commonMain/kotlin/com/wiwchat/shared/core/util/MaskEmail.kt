package com.wiwchat.shared.core.util

fun maskEmail(email: String): String {
    val parts = email.split("@")
    if (parts.size != 2) return email

    val name = parts[0]
    val domain = parts[1]

    return when {
        name.length <= 2 -> "$name***@$domain"
        name.length <= 4 -> "${name.take(1)}***${name.takeLast(1)}@$domain"
        else -> "${name.take(2)}***${name.takeLast(2)}@$domain"
    }
}