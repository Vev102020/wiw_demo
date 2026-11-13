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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiwchat.features.auth.presentation.viewmodel.ResetPasswordViewModel
import com.wiwchat.core.ui.components.base.InputField
import com.wiwchat.core.ui.components.base.PrimaryButton
import com.wiwchat.core.ui.theme.*
import com.wiwchat.core.ui.util.mapAuthErrorToId

@Composable
fun ResetPasswordRoute(
    viewModel: ResetPasswordViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler(enabled = true) { onNavigateBack() }

    val newPasswordText by viewModel.newPasswordInput.text.collectAsStateWithLifecycle()
    val newPasswordErrorCode by viewModel.newPasswordInput.error.collectAsStateWithLifecycle()

    val newPasswordRepeatText by viewModel.newPasswordRepeatInput.text.collectAsStateWithLifecycle()
    val newPasswordRepeatErrorCode by viewModel.newPasswordRepeatInput.error.collectAsStateWithLifecycle()

    // Разворачиваем общие состояния экрана
    val isButtonEnabled by viewModel.isButtonEnabled.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    ResetPasswordScreen(
        newPasswordText = newPasswordText,
        newPasswordError = newPasswordErrorCode?.let { mapAuthErrorToId(it) },
        newPasswordRepeatText = newPasswordRepeatText,
        newPasswordRepeatError = newPasswordRepeatErrorCode?.let { mapAuthErrorToId(it) },
        isButtonEnabled = isButtonEnabled,
        isLoading = isLoading,
        onNewPasswordChanged = { viewModel.newPasswordInput.onTextChanged(it) },
        onNewPasswordFocusChanged = { viewModel.newPasswordInput.onFocusChanged(it) },
        onNewPasswordRepeatChanged = { viewModel.newPasswordRepeatInput.onTextChanged(it) },
        onNewPasswordRepeatFocusChanged = { viewModel.newPasswordRepeatInput.onFocusChanged(it) },
        onResetClick = { viewModel.resetPasswordToServer(onNavigateToLogin) },
    )
}

@Composable
private fun ResetPasswordScreen(
    newPasswordText: String,
    newPasswordError: Int?,
    newPasswordRepeatText: String,
    newPasswordRepeatError: Int?,
    isButtonEnabled: Boolean,
    isLoading: Boolean,
    onNewPasswordChanged: (String) -> Unit,
    onNewPasswordFocusChanged: (Boolean) -> Unit,
    onNewPasswordRepeatChanged: (String) -> Unit,
    onNewPasswordRepeatFocusChanged: (Boolean) -> Unit,
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

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            // новый пароль
            InputField(
                value = newPasswordText,
                onValueChange = onNewPasswordChanged,
                label = stringResource(R.string.auth_new_password_label),
                isError = newPasswordError != null,
                errorMessage = newPasswordError?.let { stringResource(id = it) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (isPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                            ),
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { onNewPasswordFocusChanged(it.isFocused) }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // Повтор нового пароля
            InputField(
                value = newPasswordRepeatText,
                onValueChange = onNewPasswordRepeatChanged,
                label = stringResource(R.string.auth_password_label_repeat),
                isError = newPasswordRepeatError != null,
                errorMessage = newPasswordRepeatError?.let { stringResource(id = it) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = if (isPasswordRepeatVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordRepeatVisible = !isPasswordRepeatVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (isPasswordRepeatVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                            ),
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { onNewPasswordRepeatFocusChanged(it.isFocused) }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = if (isLoading) stringResource(R.string.cm_loading) else stringResource(R.string.auth_reset_btn),
                onClick = onResetClick,
                enabled = isButtonEnabled
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun ResetPasswordScreenPreview() {
    val previewNewPassword by remember { mutableStateOf("NewPassword123") }
    val previewRepeatPassword by remember { mutableStateOf("NewPassword123") }

    AppTheme {
        ResetPasswordScreen(
            newPasswordText = previewNewPassword,
            newPasswordError = null,
            newPasswordRepeatText = previewRepeatPassword,
            newPasswordRepeatError = null,
            isButtonEnabled = true,
            isLoading = false,
            onNewPasswordChanged = {},
            onNewPasswordFocusChanged = {},
            onNewPasswordRepeatChanged = {},
            onNewPasswordRepeatFocusChanged = {},
            onResetClick = {}
        )
    }
}