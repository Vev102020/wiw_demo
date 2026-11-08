package com.wiwchat.features.auth.presentation.screen

import com.wiwchat.app.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.wiwchat.features.auth.presentation.components.ValidatedInputField
import com.wiwchat.features.auth.presentation.state.PasswordFieldState
import com.wiwchat.features.auth.presentation.viewmodel.ResetPasswordViewModel
import com.wiwchat.core.ui.components.InputField
import com.wiwchat.core.ui.components.PrimaryButton
import com.wiwchat.core.ui.theme.AppTheme
import com.wiwchat.core.ui.theme.appColors
import com.wiwchat.core.ui.theme.spacing

@Composable
fun ResetPasswordRoute(
    viewModel: ResetPasswordViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler(enabled = true) { onNavigateBack() }

    ResetPasswordScreen(
        otpCodeValue = viewModel.otpCodeInput.value,
        onOtpCodeChanged = { viewModel.onOtpCodeChanged(it) },
        newPasswordValue = viewModel.newPasswordInput,
        newPasswordRepeatValue = viewModel.newPasswordRepeatInput,
        onPasswordRepeatFocusChanged = { viewModel.onPasswordRepeatFocusChanged(it) },
        isButtonEnabled = viewModel.isButtonEnabled,
        isLoading = viewModel.isLoading.value,
        errorResId = viewModel.serverErrorResId.value,
        onResetClick = { viewModel.resetPasswordToServer(onNavigateToLogin) }
    )
}

@Composable
private fun ResetPasswordScreen(
    otpCodeValue: String,
    onOtpCodeChanged: (String) -> Unit,
    newPasswordValue: PasswordFieldState,
    newPasswordRepeatValue: PasswordFieldState,
    onPasswordRepeatFocusChanged: (Boolean) -> Unit,
    isButtonEnabled: Boolean,
    isLoading: Boolean,
    errorResId: Int?,
    onResetClick: () -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordRepeatVisible by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.auth_reset_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.appColors.textDefault,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                ),
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.auth_reset_description),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.outline
                ),
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            InputField(
                value = otpCodeValue,
                onValueChange = onOtpCodeChanged,
                label = stringResource(R.string.auth_code_label),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            ValidatedInputField(
                fieldState = newPasswordValue,
                label = stringResource(R.string.auth_new_password_label), // "Придумайте новый пароль"
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val iconRes = if (isPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(painter = painterResource(id = iconRes), contentDescription = null)
                    }
                }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // 3. Поле повтора нового пароля
            ValidatedInputField(
                fieldState = newPasswordRepeatValue,
                label = stringResource(R.string.auth_password_label_repeat),
                modifier = Modifier.onFocusChanged { onPasswordRepeatFocusChanged(it.isFocused) },
                visualTransformation = if (isPasswordRepeatVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val iconRes = if (isPasswordRepeatVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                    IconButton(onClick = { isPasswordRepeatVisible = !isPasswordRepeatVisible }) {
                        Icon(painter = painterResource(id = iconRes), contentDescription = null)
                    }
                }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = if (isLoading) stringResource(R.string.cm_loading) else stringResource(R.string.auth_reset_btn),
                onClick = onResetClick,
                enabled = isButtonEnabled
            )

            errorResId?.let { resId ->
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                Text(
                    text = stringResource(id = resId),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun ResetPasswordScreenPreview() {
    val previewPasswordState = PasswordFieldState(isRegistrationMode = true).apply {
        onTextChanged("NewPassword123")
    }
    val previewPasswordRepeatState = PasswordFieldState(isRegistrationMode = false).apply {
        onTextChanged("NewPassword123")
    }

    AppTheme {
        ResetPasswordScreen(
            otpCodeValue = "123456",
            onOtpCodeChanged = {},
            newPasswordValue = previewPasswordState,
            newPasswordRepeatValue = previewPasswordRepeatState,
            onPasswordRepeatFocusChanged = {},
            isButtonEnabled = true,
            isLoading = false,
            errorResId = null,
            onResetClick = {}
        )
    }
}