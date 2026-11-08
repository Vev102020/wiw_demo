package com.wiwchat.features.auth.presentation.screen

import com.wiwchat.app.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.wiwchat.features.auth.presentation.state.EmailFieldState
import com.wiwchat.features.auth.presentation.viewmodel.ForgotViewModel
import com.wiwchat.core.ui.theme.AppTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.wiwchat.core.ui.components.AuthNavLink
import com.wiwchat.core.ui.components.PrimaryButton
import com.wiwchat.core.ui.theme.appColors
import com.wiwchat.core.ui.theme.spacing
import com.wiwchat.features.auth.presentation.components.ValidatedInputField

@Composable
fun ForgotRoute(
    viewModel: ForgotViewModel,
    onNavigateToResetVerification: (email: String, resetToken: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler(enabled = true) { onNavigateBack() }

    ForgotScreen(
        emailValue = viewModel.emailInput,
        isButtonEnabled = viewModel.isButtonEnabled,
        isLoading = viewModel.isLoading.value,
        errorResId = viewModel.serverErrorResId.value,
        onSendClick = { viewModel.sendResetCode(onNavigateToResetVerification) },
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
private fun ForgotScreen(
    emailValue: EmailFieldState,
    isButtonEnabled: Boolean,
    isLoading: Boolean,
    errorResId: Int?,
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

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // Наш переиспользуемый инпут с автоматической валидацией и фокусом
            ValidatedInputField(
                fieldState = emailValue,
                label = stringResource(R.string.auth_email_label)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = if (isLoading)
                    stringResource(R.string.cm_loading)
                else
                    stringResource(R.string.auth_forgot_send_btn),
                onClick = onSendClick,
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
    val previewEmailState = EmailFieldState().apply {
        onTextChanged("user@example.com")
    }

    AppTheme {
        ForgotScreen(
            emailValue = previewEmailState,
            isButtonEnabled = true,
            isLoading = false,
            errorResId = null,
            onSendClick = {},
            onNavigateToLogin = {}
        )
    }
}