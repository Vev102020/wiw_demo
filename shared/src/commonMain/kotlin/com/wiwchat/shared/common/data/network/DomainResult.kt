package com.wiwchat.shared.common.data.network

import com.wiwchat.shared.common.data.errors.ApiErrorCode

sealed class AppError {
    object NetworkUnavailable : AppError()
    object Timeout : AppError()
    object ServerMaintenance : AppError()
    data class Backend(val code: ApiErrorCode) : AppError()
    object Unknown : AppError()
}


sealed class DomainResult<out T> {
    data class Success<out T>(val value: T) : DomainResult<T>()
    data class Failure(val error: AppError) : DomainResult<Nothing>()
}