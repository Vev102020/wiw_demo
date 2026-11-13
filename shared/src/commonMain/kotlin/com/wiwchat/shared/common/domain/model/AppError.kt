package com.wiwchat.shared.common.domain.model

import com.wiwchat.shared.common.domain.errors.ApiErrorCode

sealed class AppError {
    object NetworkUnavailable : AppError()
    object ServerUnreachable : AppError()
    object Timeout : AppError()
    object ServerMaintenance : AppError()
    data class Backend(val code: ApiErrorCode) : AppError()
    object Unknown : AppError()
}
