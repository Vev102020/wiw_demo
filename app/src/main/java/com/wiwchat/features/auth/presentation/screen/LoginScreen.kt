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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiwchat.core.ui.components.auth.AuthNavLink
import com.wiwchat.core.ui.components.base.InputField
import com.wiwchat.core.ui.components.base.PrimaryButton
import com.wiwchat.core.ui.theme.*
import com.wiwchat.core.ui.util.mapAuthErrorToId
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

    val emailText by viewModel.emailInput.text.collectAsStateWithLifecycle()
    val emailErrorCode by viewModel.emailInput.error.collectAsStateWithLifecycle()

    val passwordText by viewModel.passwordInput.text.collectAsStateWithLifecycle()
    val passwordErrorCode by viewModel.passwordInput.error.collectAsStateWithLifecycle()

    val isButtonEnabled by viewModel.isButtonEnabled.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()


    LoginScreen(
        emailText = emailText,
        emailError = emailErrorCode?.let { mapAuthErrorToId(it) },
        passwordText = passwordText,
        passwordError = passwordErrorCode?.let { mapAuthErrorToId(it) },
        isButtonEnabled = isButtonEnabled,
        isLoading = isLoading,
        onEmailChanged = { viewModel.emailInput.onTextChanged(it) },
        onEmailFocusChanged = { viewModel.emailInput.onFocusChanged(it) },
        onPasswordChanged = { viewModel.passwordInput.onTextChanged(it) },
        onPasswordFocusChanged = { viewModel.passwordInput.onFocusChanged(it) },
        onLoginClick = { viewModel.loginToServer(onNavigateToVerification) },
        onNavigateToRegister = onNavigateToRegister,
        onForgotClick = onNavigateToForgot
    )
}


@Composable
private fun LoginScreen (
    emailText: String,
    emailError: Int?,
    passwordText: String,
    passwordError: Int?,
    isButtonEnabled: Boolean,
    isLoading: Boolean,
    onEmailChanged: (String) -> Unit,
    onEmailFocusChanged: (Boolean) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordFocusChanged: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotClick: () -> Unit
){

    var isPasswordVisible by remember { mutableStateOf(false) }


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

            // password
            InputField(
                value = passwordText,
                onValueChange = onPasswordChanged,
                label = stringResource(R.string.auth_password_label),
                isError = passwordError != null,
                errorMessage = passwordError?.let { stringResource(id = it) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = if (isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
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
    val previewEmailState by remember { mutableStateOf("username@mail.com") }
    val previewPasswordState by remember { mutableStateOf("password123") }

    AppTheme {
        LoginScreen(
            emailText = previewEmailState,
            emailError = null,
            passwordText = previewPasswordState,
            passwordError = R.string.err_auth_pass_length,
            isButtonEnabled = false,
            isLoading = false,
            onEmailChanged = {},
            onEmailFocusChanged = {},
            onPasswordChanged = {},
            onPasswordFocusChanged = {},
            onLoginClick = {},
            onNavigateToRegister = {},
            onForgotClick = {}
        )
    }

}