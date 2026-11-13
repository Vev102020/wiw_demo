package com.wiwchat.core.ui.components.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiwchat.core.ui.theme.spacing
import com.wiwchat.shared.core.notification.NotificationManager
import com.wiwchat.shared.core.notification.SystemNotification

@Composable
fun MainRootContainer(
    content: @Composable () -> Unit,
    notificationManager: NotificationManager
) {
    // Наблюдаем за состоянием глобальной ошибки
    val currentNotification by notificationManager.currentNotification.collectAsStateWithLifecycle()
    var lastNonNullNotification by remember { mutableStateOf<SystemNotification?>(null) }

    if (currentNotification != null) {
        lastNonNullNotification = currentNotification
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Отрисовка основного контента приложения (в него прокинем граф навигации)
        content()

        // Глобальный оверлей для системных уведомлений банка поверх всех экранов
        AnimatedVisibility(
            visible = currentNotification != null,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = 300)),
            modifier = Modifier.fillMaxSize()
        ) {
            lastNonNullNotification?.let { notification ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = MaterialTheme.spacing.large)
                ) {
                    SystemNotificationBanner(
                        notification = notification,
                        onDismiss = { notificationManager.dismiss() },
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}