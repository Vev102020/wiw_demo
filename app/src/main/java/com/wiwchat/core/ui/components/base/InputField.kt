package com.wiwchat.core.ui.components.base

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wiwchat.app.R
import com.wiwchat.core.ui.theme.AppTheme
import com.wiwchat.core.ui.theme.components
import com.wiwchat.core.ui.theme.spacing

@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,

    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(MaterialTheme.components.standardInput.height),
            textStyle = MaterialTheme.components.standardInput.textStyle(),
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            singleLine = true,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = visualTransformation,
                    interactionSource = remember { MutableInteractionSource() },
                    placeholder = {
                        placeholder?.let { Text(text = it) }
                    },
                    label = {
                        label?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    isError = isError,
                    colors = MaterialTheme.components.standardInput.colors(),
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = true,
                            isError = isError,
                            interactionSource = remember { MutableInteractionSource() },
                            colors = MaterialTheme.components.standardInput.colors(),
                            shape = MaterialTheme.components.standardInput.shape(),
                            focusedBorderThickness = 2.dp,
                            unfocusedBorderThickness = 1.dp,
                        )
                    },
                    contentPadding = PaddingValues(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = 0.dp
                    )
                )
            }
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true, locale="en")
@Composable
private fun InputFieldPreview(){
    var emailValueEmpty by remember { mutableStateOf("") }
    var emailValue by remember { mutableStateOf("test@mail.ru") }
    var emailValueError by remember { mutableStateOf("test8.ru") }
    var errorMess by remember { mutableStateOf("Некорректная почта") }

    AppTheme {
        Scaffold(

        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues)
                    .padding(MaterialTheme.spacing.medium)
            ) {


                InputField(
                    value = emailValueEmpty,
                    onValueChange = { emailValueEmpty = it },
                    label = stringResource(R.string.auth_email_label),
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                InputField(
                    value = emailValueError,
                    onValueChange = { emailValueError = it },
                    label = stringResource(R.string.auth_email_label),
                    isError = true,
                    errorMessage = errorMess
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                InputField(
                    value = emailValue,
                    onValueChange = { emailValue = it },
                    label = stringResource(R.string.auth_email_label),
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                InputFieldPassword()
            }

        }

    }
}

@Composable
private fun InputFieldPassword(){
    var passwordValue by remember { mutableStateOf("secret_pass") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    InputField(
        value = passwordValue,
        onValueChange = { passwordValue = it },
        label = stringResource(R.string.auth_password_label),
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
        }
    )
}