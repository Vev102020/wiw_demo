package com.wiwchat.features.settings.screen

import com.wiwchat.app.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiwchat.core.ui.components.base.InputField
import com.wiwchat.core.ui.components.base.PrimaryButton
import com.wiwchat.core.ui.theme.*
import com.wiwchat.core.ui.util.mapAuthErrorToId
import com.wiwchat.features.settings.viewmodel.ChangePasswordViewModel

@Composable
fun ChangePasswordRoute(
    viewModel: ChangePasswordViewModel,
    onNavigateBack: () -> Unit
) {
    val oldPasswordText by viewModel.oldPasswordInput.text.collectAsStateWithLifecycle()
    val oldPasswordErrorCode by viewModel.oldPasswordInput.error.collectAsStateWithLifecycle()

    val newPasswordText by viewModel.newPasswordInput.text.collectAsStateWithLifecycle()
    val newPasswordErrorCode by viewModel.newPasswordInput.error.collectAsStateWithLifecycle()

    val newPasswordRepeatText by viewModel.newPasswordRepeatInput.text.collectAsStateWithLifecycle()
    val newPasswordRepeatErrorCode by viewModel.newPasswordRepeatInput.error.collectAsStateWithLifecycle()

    val isButtonEnabled by viewModel.isButtonEnabled.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    ChangePasswordScreen(
        oldPasswordText = oldPasswordText,
        oldPasswordError = oldPasswordErrorCode?.let { mapAuthErrorToId(it) },
        newPasswordText = newPasswordText,
        newPasswordError = newPasswordErrorCode?.let { mapAuthErrorToId(it) },
        newPasswordRepeatText = newPasswordRepeatText,
        newPasswordRepeatError = newPasswordRepeatErrorCode?.let { mapAuthErrorToId(it) },
        isButtonEnabled = isButtonEnabled,
        isLoading = isLoading,
        onOldPasswordChanged = { viewModel.oldPasswordInput.onTextChanged(it) },
        onOldPasswordFocusChanged = { viewModel.oldPasswordInput.onFocusChanged(it) },
        onNewPasswordChanged = { viewModel.newPasswordInput.onTextChanged(it) },
        onNewPasswordFocusChanged = { viewModel.newPasswordInput.onFocusChanged(it) },
        onNewPasswordRepeatChanged = { viewModel.newPasswordRepeatInput.onTextChanged(it) },
        onNewPasswordRepeatFocusChanged = { viewModel.newPasswordRepeatInput.onFocusChanged(it) },
        onSaveClick = { viewModel.changePasswordToServer(onNavigateBack) },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordScreen(
    oldPasswordText: String,
    oldPasswordError: Int?,
    newPasswordText: String,
    newPasswordError: Int?,
    newPasswordRepeatText: String,
    newPasswordRepeatError: Int?,
    isButtonEnabled: Boolean,
    isLoading: Boolean,
    onOldPasswordChanged: (String) -> Unit,
    onOldPasswordFocusChanged: (Boolean) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onNewPasswordFocusChanged: (Boolean) -> Unit,
    onNewPasswordRepeatChanged: (String) -> Unit,
    onNewPasswordRepeatFocusChanged: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var isOldPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isRepeatPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.auth_change_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.appColors.textDefault,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (!isLoading) onNavigateBack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = null,
                            tint = MaterialTheme.appColors.textDefault
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Старый пароль
            InputField(
                value = oldPasswordText,
                onValueChange = onOldPasswordChanged,
                label = stringResource(R.string.auth_old_password_label),
                isError = oldPasswordError != null,
                errorMessage = oldPasswordError?.let { stringResource(id = it) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = if (isOldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isOldPasswordVisible = !isOldPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (isOldPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                            ),
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { onOldPasswordFocusChanged(it.isFocused) }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // Новый пароль
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
                visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (isNewPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
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

            // 3. Повтор нового пароля
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
                visualTransformation = if (isRepeatPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isRepeatPasswordVisible = !isRepeatPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (isRepeatPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
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
                text = if (isLoading) stringResource(R.string.cm_loading) else stringResource(R.string.auth_change_save_btn),
                onClick = onSaveClick,
                enabled = isButtonEnabled
            )
        }
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun ChangePasswordScreenPreview() {

    val previewPasswordOld by remember { mutableStateOf("OldPass123") }
    val previewPasswordNew by remember { mutableStateOf("NewPass123") }
    val previewPasswordNewRepeat by remember { mutableStateOf("NewPass123") }

    AppTheme {
        ChangePasswordScreen(
            oldPasswordText = previewPasswordOld,
            oldPasswordError = null,
            newPasswordText = previewPasswordNew,
            newPasswordError = null,
            newPasswordRepeatText = previewPasswordNewRepeat,
            newPasswordRepeatError = null,
            isButtonEnabled = true,
            isLoading = false,
            onOldPasswordChanged = {},
            onOldPasswordFocusChanged = {},
            onNewPasswordChanged = {},
            onNewPasswordFocusChanged = {},
            onNewPasswordRepeatChanged = {},
            onNewPasswordRepeatFocusChanged = {},
            onSaveClick = {},
            onNavigateBack = {}
        )
    }
}