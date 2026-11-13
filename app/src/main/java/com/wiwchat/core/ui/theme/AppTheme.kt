package com.wiwchat.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
    primary = Blue300,
    onPrimary = White100,
    primaryContainer = Blue100,
    onPrimaryContainer = Black700,
    secondary = White100,
    onSecondary = Blue300,
    secondaryContainer = Blue100,
    onSecondaryContainer = Black700,
    background = White100,
    onBackground = Black700,
    surface = White100,
    onSurface = Black700,
    outline = Gray400,
    surfaceVariant = White100,
    onSurfaceVariant = Gray400,
    error = Red500,
    onError = Red500,
    errorContainer = Red500,
    onErrorContainer = Red500,
)

val DarkColorScheme = darkColorScheme(
    primary = Blue300,
    onPrimary = Black700,
    primaryContainer = Color(0x661A237E),
    onPrimaryContainer = White100,
    secondary = Gray400,
    onSecondary = White100,
    secondaryContainer = Blue300,
    onSecondaryContainer = Black700,
    background = Color(0xFF121214),
    onBackground = White100,
    surface = Color(0xFF1E1E22),
    onSurface = White100,
    outline = Color(0x66AEAEB5),
    surfaceVariant = Color(0xFF2A2A30),
    onSurfaceVariant = Gray400
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val components = if (darkTheme) DarkComponentStyles else LightComponentStyles
    val colors = if (darkTheme) DarkAppColors else LightAppColors

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalComponentStyles provides components,
        LocalColors provides colors,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }


}