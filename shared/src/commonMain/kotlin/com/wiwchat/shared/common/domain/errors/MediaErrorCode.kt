package com.wiwchat.shared.common.domain.errors

enum class MediaErrorCode : ApiErrorCode {
    // Медиа / Загрузка файлов
    MEDIA_FILE_NOT_FOUND,
    MEDIA_UPLOAD_FAILED,
    MEDIA_TOO_LARGE;

    companion object {
        fun find(name: String): ApiErrorCode? = entries.find { it.name == name }
    }
}