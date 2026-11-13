package com.wiwchat.features.profile.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wiwchat.app.R
import com.wiwchat.core.ui.components.base.InputField
import com.wiwchat.core.ui.components.base.PrimaryButton
import com.wiwchat.core.ui.theme.*
import com.wiwchat.core.ui.util.mapProfileErrorToId
import com.wiwchat.features.profile.presentation.viewmodel.SetupProfileViewModel

@Composable
fun SetupProfileRoute(
    viewModel: SetupProfileViewModel
) {

    BackHandler(enabled = true) {}

    val displayNameText by viewModel.displayNameInput.text.collectAsStateWithLifecycle()
    val displayNameErrorCode by viewModel.displayNameInput.error.collectAsStateWithLifecycle()

    val usernameText by viewModel.usernameInput.text.collectAsStateWithLifecycle()
    val usernameErrorCode by viewModel.usernameInput.error.collectAsStateWithLifecycle()

    val isButtonEnabled by viewModel.isButtonEnabled.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    SetupProfileScreen(
        displayNameText = displayNameText,
        displayNameError = displayNameErrorCode?.let { mapProfileErrorToId(it) },
        usernameText = usernameText,
        usernameError = usernameErrorCode?.let { mapProfileErrorToId(it) },
        isButtonEnabled = isButtonEnabled,
        isLoading = isLoading,
        onDisplayNameChanged = { viewModel.displayNameInput.onTextChanged(it) },
        onDisplayNameFocusChanged = { viewModel.displayNameInput.onFocusChanged(it) },
        onUsernameChanged = { viewModel.usernameInput.onTextChanged(it) },
        onUsernameFocusChanged = { viewModel.usernameInput.onFocusChanged(it) },
        onContinueClick = { viewModel.saveProfileSetup() }
    )
}

@Composable
private fun SetupProfileScreen(
    displayNameText: String,
    displayNameError: Int?,
    usernameText: String,
    usernameError: Int?,
    isButtonEnabled: Boolean,
    isLoading: Boolean,
    onDisplayNameChanged: (String) -> Unit,
    onDisplayNameFocusChanged: (Boolean) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onUsernameFocusChanged: (Boolean) -> Unit,
    onContinueClick: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.profile_setup_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.appColors.textDefault,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.profile_setup_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.appColors.textSecondary
                )
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            // Имя
            InputField(
                value = displayNameText,
                onValueChange = onDisplayNameChanged,
                label = stringResource(R.string.profile_display_name_label),
                isError = displayNameError != null,
                errorMessage = displayNameError?.let { stringResource(id = it) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { onDisplayNameFocusChanged(it.isFocused) }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            //  username
            InputField(
                value = usernameText,
                onValueChange = onUsernameChanged,
                label = stringResource(R.string.profile_username_label),
                isError = usernameError != null,
                errorMessage = usernameError?.let { stringResource(id = it) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { onUsernameFocusChanged(it.isFocused) }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = if (isLoading) {
                    stringResource(R.string.cm_loading)
                } else {
                    stringResource(R.string.profile_setup_continue_btn)
                },
                onClick = onContinueClick,
                enabled = isButtonEnabled
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun SetupProfileScreenPreview() {
    val previewDisplayName by remember { mutableStateOf("Анна") }
    val previewUsername by remember { mutableStateOf("user_94827") }

    AppTheme {
        SetupProfileScreen(
            displayNameText = previewDisplayName,
            displayNameError = null,
            usernameText = previewUsername,
            usernameError = null,
            isButtonEnabled = true,
            isLoading = false,
            onDisplayNameChanged = {},
            onDisplayNameFocusChanged = {},
            onUsernameChanged = {},
            onUsernameFocusChanged = {},
            onContinueClick = {}
        )
    }
}