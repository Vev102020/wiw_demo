package com.wiwchat.shared.common.domain.model

sealed class DomainResult<out T> {
    data class Success<out T>(val value: T) : DomainResult<T>()
    data class Failure(val error: AppError) : DomainResult<Nothing>()
}