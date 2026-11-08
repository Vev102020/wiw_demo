package com.wiwchat.features.auth.presentation.screen

import com.wiwchat.app.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.*
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.wiwchat.features.auth.presentation.viewmodel.VerificationViewModel
import com.wiwchat.core.ui.components.PrimaryButton
import com.wiwchat.core.ui.theme.AppTheme
import com.wiwchat.core.ui.theme.appColors
import com.wiwchat.core.ui.theme.spacing
import com.wiwchat.shared.core.util.maskEmail
import com.wiwchat.shared.features.auth.domain.constants.AuthConstants

@Composable
fun VerificationRoute(
    viewModel: VerificationViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler(enabled = true) { onNavigateBack() }

    VerificationScreen(
        maskedEmail = maskEmail(viewModel.email),
        otpCodeValue = viewModel.otpCodeInput.value,
        isLoading = viewModel.isLoading.value,
        errorResId = viewModel.errorStringResId.value,
        codeLength = AuthConstants.CODE_LENGTH,
        totalSecondsRemaining = viewModel.secondsRemaining.value,
        onOtpCodeChanged = { viewModel.onOtpCodeChanged(it) },
        onVerifyClick = { viewModel.verifyCodeToServer() },
        onResendCodeClick = { viewModel.startTimer() },
    )
}

@Composable
fun VerificationScreen(
    maskedEmail: String,
    otpCodeValue: String,
    onOtpCodeChanged: (String) -> Unit,
    isLoading: Boolean,
    errorResId: Int?,
    codeLength: Int,
    totalSecondsRemaining: Int,
    onVerifyClick: () -> Unit,
    onResendCodeClick: () -> Unit,
) {

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = MaterialTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.auth_verify_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.appColors.textDefault,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                ),
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            val prefix = stringResource(id = R.string.auth_verify_subtitle)

            val styledText = buildAnnotatedString {
                append(prefix)
                append(" ")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.appColors.textDefault,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append(maskedEmail)
                }
            }

            Text(
                text = styledText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.appColors.textSecondary,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            VerificationCodeInput(
                codeLength = codeLength,
                codeValue = otpCodeValue,
                isError = errorResId != null,
                enabled = !isLoading,
                onCodeChanged = onOtpCodeChanged
            )

            if (errorResId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = errorResId),
                    color = MaterialTheme.appColors.errorTextDefault,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            PrimaryButton(
                text = stringResource(R.string.auth_verify_continue_btn),
                onClick = onVerifyClick,
                enabled = otpCodeValue.length == codeLength && !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            if (totalSecondsRemaining  > 0) {
                val seconds = totalSecondsRemaining % 60

                Text(
                    text = stringResource(R.string.auth_verify_expiry, seconds),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.appColors.textSecondary,
                        fontWeight = FontWeight.Medium,
                    )
                )
            } else {
                Text(
                    text = stringResource(R.string.auth_verify_resend_btn),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.clickable(enabled = !isLoading) {
                        onResendCodeClick()
                    }
                )

            }


        }
    }

}

@Composable
private fun VerificationCodeInput(
    codeLength: Int,
    codeValue: String,
    isError: Boolean,
    enabled: Boolean,
    onCodeChanged: (String) -> Unit
){
    val focusRequesters = remember { List(codeLength) { FocusRequester() } }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))

            for (i in 0 until codeLength) {
                // Берем символ конкретно для текущей ячейки
                val charForCell = codeValue.getOrNull(i)?.toString() ?: ""

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.appColors.borderDefault,
                            shape = MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicTextField(
                        value = charForCell,
                        onValueChange = { newValue ->
                            if (!enabled) return@BasicTextField

                            // Оставляем только цифры
                            val filtered = newValue.filter { it.isDigit() }

                            if (filtered.isNotEmpty()) {
                                // Ввод символа
                                val newChar = filtered.last()
                                val builder = StringBuilder(codeValue)

                                if (i < codeValue.length) {
                                    builder.setCharAt(i, newChar)
                                } else if (i == codeValue.length) {
                                    builder.append(newChar)
                                }

                                val updatedCode = builder.toString().take(codeLength)
                                onCodeChanged(updatedCode)

                                // Фокус летит вперед
                                if (i < codeLength - 1) {
                                    focusRequesters[i + 1].requestFocus()
                                }
                            } else {
                                // Стирание символа
                                if (i < codeValue.length) {
                                    val builder = StringBuilder(codeValue)
                                    builder.deleteCharAt(i)
                                    onCodeChanged(builder.toString())
                                }

                                // Фокус возвращается назад
                                if (i > 0) {
                                    focusRequesters[i - 1].requestFocus()
                                }
                            }
                        },
                        enabled = enabled,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = if (isError) MaterialTheme.appColors.errorTextDefault else MaterialTheme.appColors.textDefault,
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequesters[i]),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                innerTextField()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }

}



@Preview(showBackground = true, locale="ru")
@Composable
private fun VerificationScreenPreview(){
    AppTheme {
        VerificationScreen(
            maskedEmail = "em**@mail.ru",
            otpCodeValue = "123",
            onOtpCodeChanged = {},
            isLoading = false,
//            errorResId = null,
            errorResId = R.string.err_auth_invalid_code,
            codeLength = 6,
            totalSecondsRemaining = 45,
            onVerifyClick = {},
            onResendCodeClick = {}
        )
    }

}

@Preview(showBackground = true, locale="ru")
@Composable
private fun VerificationCodeInputPreview(){
    val codeLength = 6
    var codeValue by remember { mutableStateOf("1234") }
    var isError by remember { mutableStateOf(true) }

    AppTheme {
        Box(
            modifier =
                Modifier
                    .background(MaterialTheme.appColors.backgroundPrimary)
                    .padding(MaterialTheme.spacing.medium)
        ) {
            VerificationCodeInput(
                codeLength = codeLength,
                codeValue = codeValue,
                isError = isError,
                enabled = true,
                onCodeChanged = { updatedCode ->
                    codeValue = updatedCode
                }
            )
        }
    }

}