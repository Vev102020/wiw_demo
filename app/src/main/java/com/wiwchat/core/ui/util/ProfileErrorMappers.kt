package com.wiwchat.core.ui.util

import com.wiwchat.app.R
import com.wiwchat.shared.common.domain.errors.ProfileErrorCode

fun mapProfileErrorToId(error: ProfileErrorCode): Int {
    return when (error) {
        ProfileErrorCode.PROFILE_EMPTY_DISPLAY_NAME -> R.string.err_profile_empty_display_name
        ProfileErrorCode.PROFILE_DISPLAY_NAME_INVALID_LENGTH -> R.string.err_profile_display_name_length
        ProfileErrorCode.PROFILE_EMPTY_USERNAME -> R.string.err_profile_empty_username
        ProfileErrorCode.PROFILE_USERNAME_INVALID_LENGTH -> R.string.err_profile_username_length
        ProfileErrorCode.PROFILE_USERNAME_INVALID_FORMAT -> R.string.err_profile_username_format
        ProfileErrorCode.PROFILE_USERNAME_ALREADY_TAKEN -> R.string.err_profile_username_taken
    }
}