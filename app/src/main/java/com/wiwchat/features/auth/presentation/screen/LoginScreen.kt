package com.wiwchat.features.auth.presentation.screen

import com.wiwchat.app.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.wiwchat.core.ui.components.AuthNavLink
import com.wiwchat.core.ui.components.PrimaryButton
import com.wiwchat.core.ui.theme.AppTheme
import com.wiwchat.core.ui.theme.appColors
import com.wiwchat.core.ui.theme.spacing
import com.wiwchat.features.auth.presentation.components.ValidatedInputField
import com.wiwchat.features.auth.presentation.state.EmailFieldState
import com.wiwchat.features.auth.presentation.state.PasswordFieldState
import com.wiwchat.features.auth.presentation.viewmodel.LoginViewModel

@Composable
fun LoginRoute(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgot: () -> Unit,
    onNavigateToVerification: (String) -> Unit,
    onNavigateBack: () -> Unit
) {

    BackHandler(enabled = true) { onNavigateBack() }

    LoginScreen(
        emailValue = viewModel.emailInput,
        passwordValue = viewModel.passwordInput,
        isButtonEnabled = viewModel.isButtonEnabled,
        isLoading = viewModel.isLoading.value,
        errorResId = viewModel.serverErrorResId.value,
        onLoginClick = { viewModel.loginToServer(onNavigateToVerification) },
        onNavigateToRegister = onNavigateToRegister,
        onForgotClick = onNavigateToForgot
    )
}


@Composable
private fun LoginScreen (
    emailValue: EmailFieldState,
    passwordValue: PasswordFieldState,
    isButtonEnabled: Boolean,
    isLoading: Boolean,
    errorResId: Int?,
    onLoginClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotClick: () -> Unit
){

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
                text = stringResource(R.string.auth_login_title),
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

            var isPasswordVisible by remember { mutableStateOf(false) }
            ValidatedInputField(
                fieldState = passwordValue,
                label = stringResource(R.string.auth_password_label),
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

            Text(
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable{ onForgotClick() },
                text = stringResource(R.string.auth_forgot_pass_btn),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.appColors.linkDefault,
                    fontWeight = FontWeight.Medium,
                )

            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = if (isLoading) stringResource(R.string.cm_loading) else stringResource(R.string.auth_login_btn),
                onClick = onLoginClick,
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

            AuthNavLink(
                baseText = stringResource(R.string.auth_login_no_account_text),
                linkText = stringResource(R.string.auth_login_link_register),
                onLinkClick = { if (!isLoading) onNavigateToRegister() },
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
            )

        }
    }


}


@Preview(showBackground = true, locale="ru")
@Composable
private fun LoginScreenPreview(){
    val previewEmailState = EmailFieldState().apply {
        onTextChanged("username@mail.com")
    }
    val previewPasswordState = PasswordFieldState(isRegistrationMode = false).apply {
        onTextChanged("password123")
    }

    AppTheme {
        LoginScreen(
            emailValue = previewEmailState,
            passwordValue = previewPasswordState,
            isButtonEnabled = true,
            isLoading = false,
            errorResId = null,
            onLoginClick = {},
            onNavigateToRegister = {},
            onForgotClick = {}
        )
    }

}