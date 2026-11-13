package com.wiwchat.shared.features.profile.domain.validator

import com.wiwchat.shared.common.domain.errors.ProfileErrorCode
import com.wiwchat.shared.features.profile.domain.constants.ProfileConstants

object ProfileValidator {
    private val usernameRegex = Regex("^[a-z0-9_]+$")

    fun validateDisplayName(name: String): ProfileErrorCode? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return ProfileErrorCode.PROFILE_EMPTY_DISPLAY_NAME
        if (trimmed.length > ProfileConstants.DISPLAY_NAME_MAX_LENGTH) {
            return ProfileErrorCode.PROFILE_DISPLAY_NAME_INVALID_LENGTH
        }
        return null
    }

    fun validateUsername(username: String): ProfileErrorCode? {
        val trimmed = username.trim().lowercase()
        if (trimmed.isEmpty()) return ProfileErrorCode.PROFILE_EMPTY_USERNAME
        if (trimmed.length !in ProfileConstants.USERNAME_MIN_LENGTH..ProfileConstants.USERNAME_MAX_LENGTH) {
            return ProfileErrorCode.PROFILE_USERNAME_INVALID_LENGTH
        }
        if (!usernameRegex.matches(trimmed)) {
            return ProfileErrorCode.PROFILE_USERNAME_INVALID_FORMAT
        }
        return null
    }
}