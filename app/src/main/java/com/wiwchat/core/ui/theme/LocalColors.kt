package com.wiwchat.core.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlin.collections.copy

//Semantic Tokens
@Immutable
data class AppColors(
    //circle
    val circle1 : Color,
    val circle2 : Color,
    //disabled
    val disabledContainer: Color,
    val disabledContent: Color,
    //error
    val errorTextDefault: Color,
    val errorContainer: Color,
    val errorContent: Color,
    //bg
    val backgroundPrimary: Color,   
    val backgroundSecondary: Color,
    val shadow: Color,
    //buttons
    val buttonContainerPrimary: Color,
    val buttonContentPrimary: Color,
    val buttonBorderPrimary: Color,
    val buttonContainerPrimaryFocused: Color,
    val buttonContentPrimaryFocused: Color,
    val buttonBorderPrimaryFocused: Color,
    val buttonContainerPrimaryInvert: Color,
    val buttonContentPrimaryInvert: Color,
    val buttonBorderPrimaryInvert: Color,

    val buttonContainerSecondary: Color,
    val buttonContentSecondary: Color,
    val buttonBorderSecondary: Color,
    val buttonContainerSecondaryFocused: Color,
    val buttonContentSecondaryFocused: Color,
    val buttonBorderSecondaryFocused: Color,
    val buttonContainerSecondaryInvert: Color,
    val buttonContentSecondaryInvert: Color,
    val buttonBorderSecondaryInvert: Color,

    // text
    val textDefault: Color,
    val textPrimary: Color,       
    val textSecondary: Color,       
    val textInvert: Color,
    // link
    val linkDefault: Color,
    // input
    val borderDefault: Color,
    val borderFocused: Color,
    // checkbox
    val checkboxDefault: Color,
    val checkboxActive: Color,
    val checkboxBorderDefault: Color,
    val checkboxBorderActive: Color,
    val checkboxIcon: Color,

    // Status
    val statusSuccess: Color,       
    val statusError: Color,
    val statusChat: Color,
    val statusGroup: Color,
)

val LightAppColors = AppColors(
    //circle
    circle1 = Blue300,
    circle2 = Blue100,
    //disabled
    disabledContainer = Gray100,
    disabledContent = Gray400,
    //error
    errorTextDefault = Red500,
    errorContainer = White100,
    errorContent = Red500,
    //bg
    backgroundPrimary = White100,
    backgroundSecondary = Gray300,
    shadow = Black700,
    //buttons
    buttonContainerPrimary = Blue300,
    buttonContentPrimary = White100,
    buttonBorderPrimary = Blue300,
    buttonContainerPrimaryFocused = Blue100,
    buttonContentPrimaryFocused = White100,
    buttonBorderPrimaryFocused = Blue100,
    buttonContainerPrimaryInvert = White100,
    buttonContentPrimaryInvert = Blue300,
    buttonBorderPrimaryInvert = White100,

    buttonContainerSecondary = Color.Transparent,
    buttonContentSecondary = Blue300,
    buttonBorderSecondary = Blue300,
    buttonContainerSecondaryFocused = Color.Transparent,
    buttonContentSecondaryFocused = Blue100,
    buttonBorderSecondaryFocused = Blue100,
    buttonContainerSecondaryInvert = Color.Transparent,
    buttonContentSecondaryInvert = White100,
    buttonBorderSecondaryInvert = White100,
    // text
    textDefault = Black700,
    textPrimary = Blue300,
    textSecondary = Gray400,
    textInvert = White100,
    // link
    linkDefault = Blue300,
    // input
    borderDefault = Gray200,
    borderFocused = Black700,
    // checkbox
    checkboxDefault = Color.Transparent,
    checkboxActive = Black700,
    checkboxBorderDefault = Gray200,
    checkboxBorderActive = Black700,
    checkboxIcon = White100,
    // status
    statusSuccess = Green400,
    statusError = Red500,
    statusChat = Blue300,
    statusGroup = Orange300,
)

val DarkAppColors = AppColors(
    //circle
    circle1 = Blue300,
    circle2 = Blue100,
    //disabled
    disabledContainer = Gray100,
    disabledContent = Gray400,
    //error
    errorTextDefault = Red500,
    errorContainer = Red500,
    errorContent = Red500,
    //bg
    backgroundPrimary = White100,
    backgroundSecondary = Gray10,
    shadow = Black700,
    //buttons
    buttonContainerPrimary = Blue300,
    buttonContentPrimary = White100,
    buttonBorderPrimary = Blue300,
    buttonContainerPrimaryFocused = Blue100,
    buttonContentPrimaryFocused = White100,
    buttonBorderPrimaryFocused = Blue100,
    buttonContainerPrimaryInvert = White100,
    buttonContentPrimaryInvert = Blue300,
    buttonBorderPrimaryInvert = White100,

    buttonContainerSecondary = Color.Transparent,
    buttonContentSecondary = Blue300,
    buttonBorderSecondary = Blue300,
    buttonContainerSecondaryFocused = Color.Transparent,
    buttonContentSecondaryFocused = Blue100,
    buttonBorderSecondaryFocused = Blue100,
    buttonContainerSecondaryInvert = Color.Transparent,
    buttonContentSecondaryInvert = White100,
    buttonBorderSecondaryInvert = White100,
    // text
    textDefault = Black700,
    textPrimary = Blue300,
    textSecondary = Gray400,
    textInvert = White100,
    // link
    linkDefault = Blue300,
    // input
    borderDefault = Gray200,
    borderFocused = Blue300,
    // checkbox
    checkboxDefault = Color.Transparent,
    checkboxActive = Black700,
    checkboxBorderDefault = Gray200,
    checkboxBorderActive = Black700,
    checkboxIcon = White100,
    // status
    statusSuccess = Green400,
    statusError = Red500,
    statusChat = Blue300,
    statusGroup = Orange300,
)

val LocalColors = staticCompositionLocalOf { LightAppColors }

val MaterialTheme.appColors: AppColors
    @Composable
    get() = LocalColors.current