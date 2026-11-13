package com.wiwchat.core.ui.components.base

import com.wiwchat.app.R
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import com.wiwchat.core.ui.theme.appColors

@Composable
fun RoundCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.appColors.checkboxActive else MaterialTheme.appColors.checkboxDefault,
    )

    val borderColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.appColors.checkboxBorderActive else MaterialTheme.appColors.checkboxBorderDefault,
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = MaterialTheme.appColors.checkboxIcon,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}