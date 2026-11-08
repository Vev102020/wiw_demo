package com.wiwchat.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import com.wiwchat.core.ui.theme.appColors

@Composable
fun AuthNavLink(
    baseText: String,
    linkText: String,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(
            color = MaterialTheme.appColors.textSecondary,
            fontWeight = FontWeight.Medium
        )) {
            append(baseText)
        }

        append(" ")

        withStyle(style = SpanStyle(
            color = MaterialTheme.appColors.linkDefault,
            fontWeight = FontWeight.SemiBold
        )) {
            append(linkText)
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onLinkClick()
            }
        )
    }
}