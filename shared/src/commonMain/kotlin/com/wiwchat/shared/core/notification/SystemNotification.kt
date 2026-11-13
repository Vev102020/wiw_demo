package com.wiwchat.shared.core.notification

import com.wiwchat.shared.common.domain.model.AppError

data class SystemNotification(
    val id: Long,
    val error: AppError
)