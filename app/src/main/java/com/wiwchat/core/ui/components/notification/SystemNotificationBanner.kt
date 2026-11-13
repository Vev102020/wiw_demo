package com.wiwchat.core.ui.components.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.wiwchat.core.ui.theme.*
import com.wiwchat.core.ui.util.mapAppErrorToId
import com.wiwchat.shared.common.domain.model.AppError
import com.wiwchat.shared.core.notification.SystemNotification
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SystemNotificationBanner(
    notification: SystemNotification,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Автоматическое скрытие баннера через 3 секунды
    LaunchedEffect(notification.id) {
        delay(3000.milliseconds)
        onDismiss()
    }

    val errorStringRes = mapAppErrorToId(notification.error)
    val message = stringResource(id = errorStringRes)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = MaterialTheme.spacing.small,
                end = MaterialTheme.spacing.small,
                bottom = MaterialTheme.spacing.extraLarge,
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = MaterialTheme.shapes.extraLarge,
                    clip = false,
                    ambientColor = MaterialTheme.appColors.shadow.copy(alpha = 0.5f),
                    spotColor = MaterialTheme.appColors.shadow.copy(alpha = 0.5f),
                )
                .clip(MaterialTheme.shapes.extraLarge,)
                .background(MaterialTheme.appColors.backgroundPrimary)
                .padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SystemNotificationBannerPreview(){
    val mockNetworkNotification = SystemNotification(
        id = 1L,
        error = AppError.Timeout
    )

    // 2. Создаем фейковую ошибку обслуживания сервера
    val mockMaintenanceNotification = SystemNotification(
        id = 2L,
        error = AppError.ServerMaintenance
    )

    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.appColors.backgroundSecondary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SystemNotificationBanner(
                notification = mockNetworkNotification,
                onDismiss = {}
            )

            SystemNotificationBanner(
                notification = mockMaintenanceNotification,
                onDismiss = {}
            )
        }
    }
}