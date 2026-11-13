package com.wiwchat.shared.core.notification

import com.wiwchat.shared.common.domain.model.AppError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class NotificationManagerImpl : NotificationManager {
    private val _currentNotification = MutableStateFlow<SystemNotification?>(null)
    override val currentNotification: StateFlow<SystemNotification?> = _currentNotification.asStateFlow()

    @OptIn(ExperimentalTime::class)
    override fun showNotification(error: AppError) {
        _currentNotification.value = SystemNotification(
            id = Clock.System.now().toEpochMilliseconds(),
            error = error
        )
    }

    override fun dismiss() {
        _currentNotification.value = null
    }
}