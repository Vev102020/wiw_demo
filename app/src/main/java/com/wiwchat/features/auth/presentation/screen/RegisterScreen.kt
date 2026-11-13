package com.wiwchat.features.auth.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiwchat.app.R
import com.wiwchat.core.ui.components.auth.AuthNavLink
import com.wiwchat.core.ui.components.base.InputField
import com.wiwchat.core.ui.components.base.PolicyCheckbox
import com.wiwchat.core.ui.components.base.PrimaryButton
import com.wiwchat.core.ui.theme.AppTheme
import com.wiwchat.core.ui.theme.appColors
import com.wiwchat.core.ui.theme.spacing
import com.wiwchat.core.ui.util.mapAuthErrorToId
import com.wiwchat.features.auth.presentation.viewmodel.RegisterViewModel

@Composable
fun RegisterRoute(
    viewModel: RegisterViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToVerification: (String, String) -> Unit,
    onNavigateBack: () -> Unit
) {

    BackHandler(enabled = true) { onNavigateBack() }

    val emailText by viewModel.emailInput.text.collectAsStateWithLifecycle()
    val emailErrorCode by viewModel.emailInput.error.collectAsStateWithLifecycle()

    val passwordText by viewModel.passwordInput.text.collectAsStateWithLifecycle()
    val passwordErrorCode by viewModel.passwordInput.error.collectAsStateWithLifecycle()

    val passwordRepeatText by viewModel.passwordRepeatInput.text.collectAsStateWithLifecycle()
    val passwordRepeatErrorCode by viewModel.passwordRepeatInput.error.collectAsStateWithLifecycle()

    // Разворачиваем общие состояния экрана
    val isButtonEnabled by viewModel.isButtonEnabled.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isPolicyChecked by viewModel.isPolicyChecked.collectAsStateWithLifecycle()

    RegisterScreen(
        emailText = emailText,
        emailError = emailErrorCode?.let { mapAuthErrorToId(it) },
        passwordText = passwordText,
        passwordError = passwordErrorCode?.let { mapAuthErrorToId(it) },
        passwordRepeatText = passwordRepeatText,
        passwordRepeatError = passwordRepeatErrorCode?.let { mapAuthErrorToId(it) },
        isButtonEnabled = isButtonEnabled,
        isLoading = isLoading,
        isPolicyChecked = isPolicyChecked,
        onEmailChanged = { viewModel.emailInput.onTextChanged(it) },
        onEmailFocusChanged = { viewModel.emailInput.onFocusChanged(it) },
        onPasswordChanged = { viewModel.passwordInput.onTextChanged(it) },
        onPasswordFocusChanged = { viewModel.passwordInput.onFocusChanged(it) },
        onPasswordRepeatChanged = { viewModel.passwordRepeatInput.onTextChanged(it) },
        onPasswordRepeatFocusChanged = { viewModel.passwordRepeatInput.onFocusChanged(it) },
        onPolicyCheckedChange = { viewModel.onPolicyCheckedChange(it) },
        onRegisterClick = { viewModel.registerToServer(onNavigateToVerification) },
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
fun RegisterScreen (
    emailText: String,
    emailError: Int?,
    passwordText: String,
    passwordError: Int?,
    passwordRepeatText: String,
    passwordRepeatError: Int?,
    isButtonEnabled: Boolean,
    isLoading: Boolean,
    isPolicyChecked: Boolean,
    onEmailChanged: (String) -> Unit,
    onEmailFocusChanged: (Boolean) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordFocusChanged: (Boolean) -> Unit,
    onPasswordRepeatChanged: (String) -> Unit,
    onPasswordRepeatFocusChanged: (Boolean) -> Unit,
    onPolicyCheckedChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit
){

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordRepeatVisible by remember { mutableStateOf(false) }

    Scaffold(){
            paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = MaterialTheme.spacing.medium),
            verticalArrangement =  Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.auth_reg_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.appColors.textDefault,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                ),
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            // email
            InputField(
                value = emailText,
                onValueChange = onEmailChanged,
                label = stringResource(R.string.auth_email_label),
                isError = emailError != null,
                errorMessage = emailError?.let { stringResource(id = it) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { onEmailFocusChanged(it.isFocused) }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // пароль
            InputField(
                value = passwordText,
                onValueChange = onPasswordChanged,
                label = stringResource(R.string.auth_password_label),
                isError = passwordError != null,
                errorMessage = passwordError?.let { stringResource(id = it) },
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
                    .onFocusChanged { onPasswordFocusChanged(it.isFocused) }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            InputField(
                value = passwordRepeatText,
                onValueChange = onPasswordRepeatChanged,
                label = stringResource(R.string.auth_password_label_repeat),
                isError = passwordRepeatError != null,
                errorMessage = passwordRepeatError?.let { stringResource(id = it) },
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
                    .onFocusChanged { onPasswordRepeatFocusChanged(it.isFocused) }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            PolicyCheckbox(
                checked = isPolicyChecked,
                onCheckedChange = onPolicyCheckedChange
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = if (isLoading) stringResource(R.string.cm_loading) else stringResource(R.string.auth_reg_btn),
                onClick = onRegisterClick,
                enabled = isButtonEnabled
            )

            Spacer(modifier = Modifier.weight(1f))

            AuthNavLink(
                baseText = stringResource(R.string.auth_reg_yes_account_text),
                linkText = stringResource(R.string.auth_reg_link_login),
                onLinkClick = { if (!isLoading) onNavigateToLogin() },
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
            )
        }
    }


}

@Preview(showBackground = true, locale="en")
@Composable
private fun RegisterScreenPreview(){
    var previewEmailState by remember { mutableStateOf("username@mail.com") }
    var previewPasswordState by remember { mutableStateOf("password123") }
    var previewPasswordRepeatState by remember { mutableStateOf("password123") }

    AppTheme {
        RegisterScreen(
            emailText = previewEmailState,
            emailError = null,
            passwordText = previewPasswordState,
            passwordError = null,
            passwordRepeatText = previewPasswordRepeatState,
            passwordRepeatError = null,
            isButtonEnabled = true,
            isLoading = false,
            isPolicyChecked = true,
            onEmailChanged = {},
            onEmailFocusChanged = {},
            onPasswordChanged = {},
            onPasswordFocusChanged = {},
            onPasswordRepeatChanged = {},
            onPasswordRepeatFocusChanged = {},
            onPolicyCheckedChange = {},
            onRegisterClick = {},
            onNavigateToLogin = {}
        )
    }
}