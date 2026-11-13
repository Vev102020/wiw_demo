package com.wiwchat.shared.common.domain.errors

enum class ProfileErrorCode : ApiErrorCode {
    PROFILE_EMPTY_DISPLAY_NAME,
    PROFILE_DISPLAY_NAME_INVALID_LENGTH,
    PROFILE_EMPTY_USERNAME,
    PROFILE_USERNAME_INVALID_LENGTH,
    PROFILE_USERNAME_INVALID_FORMAT,
    PROFILE_USERNAME_ALREADY_TAKEN;

    companion object {
        fun find(name: String): ApiErrorCode? = ProfileErrorCode.entries.find { it.name == name }
    }
}