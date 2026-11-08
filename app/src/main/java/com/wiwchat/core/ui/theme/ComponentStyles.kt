package com.wiwchat.core.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

@Immutable
data class ComponentStyles (
    val primaryButton: ButtonStyle,
    val secondaryButton: ButtonStyle,

    val welcomePrimaryButton: ButtonStyle,
    val welcomeSecondaryButton: ButtonStyle,

    val standardInput: InputStyle,
)

@Immutable
data class ButtonStyle(
    val colors: @Composable () -> ButtonColors,
    val textStyle: @Composable () -> TextStyle,
    val height: Dp,
    val shape: @Composable () -> Shape,
    val border: @Composable (enabled: Boolean) -> BorderStroke? = { null }
)

@Immutable
data class InputStyle(
    val colors: @Composable () -> TextFieldColors,
    val textStyle: @Composable () -> TextStyle,
    val shape: @Composable () -> Shape,
    val height: Dp,
    val errorTextStyle: TextStyle
)

// Светлая тема
val LightComponentStyles = ComponentStyles(
    primaryButton = ButtonStyle(
        colors = {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.appColors.buttonContainerPrimary,
                contentColor = MaterialTheme.appColors.buttonContentPrimary,
                disabledContainerColor = MaterialTheme.appColors.disabledContainer,
                disabledContentColor = MaterialTheme.appColors.disabledContent,
            )
        },
        textStyle = {
            MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            )
        },
        height = 44.dp,
        shape = { MaterialTheme.shapes.extraLarge }
    ),

    secondaryButton = ButtonStyle(
        colors = {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.appColors.buttonContainerSecondary,
                contentColor = MaterialTheme.appColors.buttonContentSecondary,
                disabledContainerColor = MaterialTheme.appColors.disabledContainer,
                disabledContentColor = MaterialTheme.appColors.disabledContent,
            )
        },
        textStyle = {
            MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            )
        },
        height = 44.dp,
        shape = { MaterialTheme.shapes.extraLarge },
        border = { enabled ->
            if (enabled)
                BorderStroke(1.dp, MaterialTheme.appColors.buttonBorderSecondary)
            else
                BorderStroke(1.dp, MaterialTheme.appColors.disabledContainer)
        }
    ),

    welcomePrimaryButton = ButtonStyle(
        colors = {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.appColors.buttonContainerPrimaryInvert,
                contentColor = MaterialTheme.appColors.buttonContentPrimaryInvert,
            )
        },
        textStyle = {
            MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        },
        height = 44.dp,
        shape = { MaterialTheme.shapes.extraLarge }
    ),
    welcomeSecondaryButton = ButtonStyle(
        colors = {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.appColors.buttonContainerSecondaryInvert,
                contentColor = MaterialTheme.appColors.buttonContentSecondaryInvert,
            )
        },
        textStyle = {
            MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            )
        },
        height = 44.dp,
        shape = {
            MaterialTheme.shapes.extraLarge
        },
        border = {
            BorderStroke(1.dp, MaterialTheme.appColors.buttonBorderSecondaryInvert)
        }
    ),

    standardInput = InputStyle(
        colors = {
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue300,
                unfocusedBorderColor = Gray200,
            )
        },
        textStyle = {
            MaterialTheme.typography.bodyLarge
        },
        height = 50.dp,
        shape = { MaterialTheme.shapes.extraLarge },
        errorTextStyle = TextStyle(fontSize = 12.sp)
    )
)

// Темная тема
val DarkComponentStyles = ComponentStyles(
    primaryButton = ButtonStyle(
        colors = {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.appColors.buttonContainerPrimary,
                contentColor = MaterialTheme.appColors.buttonContentPrimary,
                disabledContainerColor = MaterialTheme.appColors.disabledContainer,
                disabledContentColor = MaterialTheme.appColors.disabledContent,
            )
        },
        textStyle = {
            MaterialTheme.typography.bodyLarge
        },
        height = 44.dp,
        shape = { MaterialTheme.shapes.extraLarge }
    ),
    secondaryButton = ButtonStyle(
        colors = {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.appColors.buttonContainerSecondary,
                contentColor = MaterialTheme.appColors.buttonContentSecondary,
                disabledContainerColor = MaterialTheme.appColors.disabledContainer,
                disabledContentColor = MaterialTheme.appColors.disabledContent,
            )
        },
        textStyle = {
            MaterialTheme.typography.bodyLarge
        },
        height = 44.dp,
        shape = {  MaterialTheme.shapes.extraLarge },
        border = { enabled ->
            if (enabled)
                BorderStroke(1.dp, Blue300)
            else
                null
        }
    ),

    welcomePrimaryButton = ButtonStyle(
        colors = {
            ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Blue300,
                disabledContainerColor = Gray100,
                disabledContentColor = Gray300
            )
        },
        textStyle = {
            MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        },
        height = 44.dp,
        shape = { MaterialTheme.shapes.extraLarge }
    ),
    welcomeSecondaryButton = ButtonStyle(
        colors = {
            ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = White100,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Gray300
            )
        },
        textStyle = {
            MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        },
        height = 44.dp,
        shape = { MaterialTheme.shapes.extraLarge },
        border = { enabled ->
            if (enabled)
                BorderStroke(1.dp, White100)
            else
                BorderStroke(1.dp, Gray300) // Белая рамка
        }
    ),

    standardInput = InputStyle(
        colors = {
            OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = Color(0xFF1E88E5),
//                unfocusedBorderColor = Color(0xFFBDBDBD))
            )
        },
        height = 50.dp,
        textStyle = {
            MaterialTheme.typography.bodyLarge
        },
        shape = { MaterialTheme.shapes.extraLarge },
        errorTextStyle = TextStyle(fontSize = 12.sp)
    )
)

val LocalComponentStyles = staticCompositionLocalOf { LightComponentStyles }

val MaterialTheme.components: ComponentStyles
    @Composable
    @ReadOnlyComposable
    get() = LocalComponentStyles.current