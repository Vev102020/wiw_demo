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
import com.wiwchat.app.R
import com.wiwchat.core.ui.components.AuthNavLink
import com.wiwchat.core.ui.components.PolicyCheckbox
import com.wiwchat.core.ui.components.PrimaryButton
import com.wiwchat.core.ui.theme.AppTheme
import com.wiwchat.core.ui.theme.appColors
import com.wiwchat.core.ui.theme.spacing
import com.wiwchat.features.auth.presentation.components.ValidatedInputField
import com.wiwchat.features.auth.presentation.state.EmailFieldState
import com.wiwchat.features.auth.presentation.state.PasswordFieldState
import com.wiwchat.features.auth.presentation.viewmodel.RegisterViewModel

@Composable
fun RegisterRoute(
    viewModel: RegisterViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToVerification: (String, String) -> Unit,
    onNavigateBack: () -> Unit
) {

    BackHandler(enabled = true) { onNavigateBack() }

    RegisterScreen(
        emailValue = viewModel.emailInput,
        passwordValue = viewModel.passwordInput,
        passwordRepeatValue = viewModel.passwordRepeatInput,
        onPasswordRepeatFocusChanged = { viewModel.onPasswordRepeatFocusChanged(it) },

        isButtonEnabled = viewModel.isButtonEnabled,
        isLoading = viewModel.isLoading.value,
        errorResId = viewModel.serverErrorResId.value,

        isPolicyChecked = viewModel.isPolicyChecked.value,
        onPolicyCheckedChange = { viewModel.onPolicyCheckedChange(it) },

        onRegisterClick = { viewModel.registerToServer(onNavigateToVerification) },
        onNavigateToLogin = onNavigateToLogin,
    )
}

@Composable
fun RegisterScreen (
    emailValue: EmailFieldState,
    passwordValue: PasswordFieldState,
    passwordRepeatValue: PasswordFieldState,
    onPasswordRepeatFocusChanged: (Boolean) -> Unit,
    isButtonEnabled: Boolean,
    isLoading: Boolean,
    errorResId: Int?,
    isPolicyChecked: Boolean,
    onPolicyCheckedChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
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
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            ValidatedInputField(
                fieldState = emailValue,
                label = stringResource(R.string.auth_email_label)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            ValidatedInputField(
                fieldState = passwordValue,
                label = stringResource(R.string.auth_password_label),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val iconRes = if (isPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = stringResource(R.string.auth_show_hide_pass_btn),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            ValidatedInputField(
                fieldState = passwordRepeatValue,
                label = stringResource(R.string.auth_password_label_repeat),
                modifier = Modifier.onFocusChanged { onPasswordRepeatFocusChanged(it.isFocused) },
                visualTransformation = if (isPasswordRepeatVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val iconRes = if (isPasswordRepeatVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                    IconButton(onClick = { isPasswordRepeatVisible = !isPasswordRepeatVisible }) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = stringResource(R.string.auth_show_hide_pass_btn),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
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

            errorResId?.let {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                Text(
                    text = stringResource(it),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.spacing.small),
                    textAlign = TextAlign.Center
                )
            }

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
    val previewEmailState = EmailFieldState().apply {
        onTextChanged("username@mail.com")
    }
    val previewPasswordState = PasswordFieldState(isRegistrationMode = true).apply {
        onTextChanged("password123")
    }
    val previewPasswordRepeatState = PasswordFieldState(isRegistrationMode = false).apply {
        onTextChanged("password123")
    }

    AppTheme {
        RegisterScreen(
            emailValue = previewEmailState,
            passwordValue = previewPasswordState,
            passwordRepeatValue = previewPasswordRepeatState,
            onPasswordRepeatFocusChanged = {},
            isButtonEnabled = true,
            isLoading = false,
            errorResId = null,
            isPolicyChecked = true,
            onPolicyCheckedChange = {},
            onRegisterClick = {},
            onNavigateToLogin = {}
        )
    }
}