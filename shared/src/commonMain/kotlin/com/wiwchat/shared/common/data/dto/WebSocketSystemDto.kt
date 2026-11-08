package com.wiwchat.shared.common.data.dto

import com.wiwchat.shared.common.data.dto.enums.WebSocketSystemAction
import kotlinx.serialization.Serializable

@Serializable
data class WebSocketSystemDto(
    val action: WebSocketSystemAction,
    val reason: String? = null
)