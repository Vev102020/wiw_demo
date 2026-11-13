package com.wiwchat.features.auth.presentation.screen

import com.wiwchat.app.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.wiwchat.core.ui.theme.AppTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiwchat.core.ui.components.auth.AuthNavLink
import com.wiwchat.core.ui.components.base.InputField
import com.wiwchat.core.ui.components.base.PrimaryButton
import com.wiwchat.core.ui.theme.*
import com.wiwchat.core.ui.util.mapAuthErrorToId
import com.wiwchat.features.auth.presentation.viewmodel.ForgotPasswordViewModel

@Composable
fun ForgotPasswordRoute(
    viewModel: ForgotPasswordViewModel,
    onNavigateToResetVerification: (email: String, resetToken: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler(enabled = true) { onNavigateBack() }

    val emailText by viewModel.emailInput.text.collectAsStateWithLifecycle()
    val emailErrorCode by viewModel.emailInput.error.collectAsStateWithLifecycle()

    val isButtonEnabled by viewModel.isButtonEnabled.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    ForgotPasswordScreen(
        emailText = emailText,
        emailError = emailErrorCode?.let { mapAuthErrorToId(it) },
        isButtonEnabled = isButtonEnabled,
        isLoading = isLoading,
        onEmailChanged = { viewModel.emailInput.onTextChanged(it) },
        onEmailFocusChanged = { viewModel.emailInput.onFocusChanged(it) },
        onSendClick = { viewModel.sendResetCode(onNavigateToResetVerification) },
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
private fun ForgotPasswordScreen(
    emailText: String,
    emailError: Int?,
    isButtonEnabled: Boolean,
    isLoading: Boolean,
    onEmailChanged: (String) -> Unit,
    onEmailFocusChanged: (Boolean) -> Unit,
    onSendClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
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
                text = stringResource(R.string.auth_forgot_title),
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
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { onEmailFocusChanged(it.isFocused) }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = if (isLoading) {
                    stringResource(R.string.cm_loading)
                } else {
                    stringResource(R.string.auth_forgot_send_btn)
                },
                onClick = onSendClick,
                enabled = isButtonEnabled
            )

            Spacer(modifier = Modifier.weight(1f))

            AuthNavLink(
                baseText = stringResource(R.string.auth_forgot_remember_pass),
                linkText = stringResource(R.string.auth_forgot_link_login),
                onLinkClick = { if (!isLoading) onNavigateToLogin() },
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
            )
        }
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun ForgotScreenPreview() {
    val previewEmailState by remember { mutableStateOf("username@mail.com") }

    AppTheme {
        ForgotPasswordScreen(
            emailText = previewEmailState,
            emailError = null,
            isButtonEnabled = true,
            isLoading = false,
            onEmailChanged = {},
            onEmailFocusChanged = {},
            onSendClick = {},
            onNavigateToLogin = {}
        )
    }
}