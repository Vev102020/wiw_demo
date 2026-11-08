package com.wiwchat.shared.common.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
)

@Serializable
data class SuccessResponse(
    val message: String
)