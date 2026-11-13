package com.wiwchat.features.auth.presentation.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.wiwchat.app.R
import com.wiwchat.core.ui.components.base.PrimaryButton
import com.wiwchat.core.ui.components.base.SecondaryButton
import com.wiwchat.core.ui.theme.AppTheme
import com.wiwchat.core.ui.theme.appColors
import com.wiwchat.core.ui.theme.components
import com.wiwchat.core.ui.theme.spacing


@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
){
        val color1  = MaterialTheme.appColors.circle1
        val color2 = MaterialTheme.appColors.circle2

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .drawBehind {


                    val path2 = Path().apply {
                        moveTo(0f, size.height)
                        lineTo(0f, size.height * 0.45f)

                        quadraticTo(
                            x1 = size.width * 0.7f,
                            y1 = size.height * 0.45f,
                            x2 = size.width,
                            y2 = size.height * (1f - 0.45f)
                        )

                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(path = path2, color = color2)

                    val path1 = Path().apply {
                        moveTo(0f, size.height)
                        lineTo(0f, size.height * 0.50f)

                        quadraticTo(
                            x1 = size.width * 0.6f,
                            y1 = size.height * 0.5f,
                            x2 = size.width,
                            y2 = size.height * (1f - 0.30f)
                        )

                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(path = path1, color = color1)
                }
        ) {
            Column (
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Spacer(modifier = Modifier.weight(1f))

                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "",
                    modifier = Modifier.size(166.dp)
                )

                Spacer(modifier = Modifier.weight(1.5f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.spacing.large),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.welcome_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.appColors.textInvert,
                            fontSize = 30.sp,
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                    Text(
                        text = stringResource(R.string.welcome_text),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.appColors.textInvert.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.extraLarge * 2
                        )
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

                    PrimaryButton(
                        text = stringResource(R.string.welcome_login_btn),
                        style = MaterialTheme.components.welcomePrimaryButton,
                        onClick = onNavigateToLogin,
                        modifier = Modifier.width(200.dp)
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    SecondaryButton(
                        text = stringResource(R.string.welcome_reg_btn),
                        style = MaterialTheme.components.welcomeSecondaryButton,
                        onClick = onNavigateToRegister,
                        modifier = Modifier.width(200.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(0.5f))
            }
        }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WelcomeScreenPreview(){
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            WelcomeScreen(
                onNavigateToLogin = { },
                onNavigateToRegister = { },
            )
        }
    }
}