package com.wiwchat.shared.core.notification

import com.wiwchat.shared.common.domain.model.AppError
import kotlinx.coroutines.flow.StateFlow

interface NotificationManager {
    val currentNotification: StateFlow<SystemNotification?>
    fun showNotification(error: AppError)
    fun dismiss()
}