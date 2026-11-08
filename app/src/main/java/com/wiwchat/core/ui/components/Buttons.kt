package com.wiwchat.core.ui.components

import com.wiwchat.app.R
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wiwchat.core.ui.theme.AppTheme
import com.wiwchat.core.ui.theme.ButtonStyle
import com.wiwchat.core.ui.theme.components

@Composable
private fun BaseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: ButtonStyle,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(style.height),
        enabled = enabled,
        shape = style.shape(),
        colors = style.colors(),
        border = style.border(enabled),
        content = content,
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: ButtonStyle = MaterialTheme.components.primaryButton
){
    BaseButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        style = style
    ) {
        Text(
            text = text,
            style = style.textStyle()
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: ButtonStyle = MaterialTheme.components.secondaryButton
) {
    BaseButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        style = style
    ) {
        Text(
            text = text,
            style = style.textStyle()
        )
    }
}

@Preview(showBackground = true, locale="en")
@Composable
private fun ButtonsPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PrimaryButton(text = stringResource(R.string.auth_login_btn), onClick = {})
                PrimaryButton(
                    text = stringResource(R.string.auth_login_btn),
                    enabled = false,
                    onClick = {})

                SecondaryButton(text = stringResource(R.string.auth_reg_btn), onClick = {})
                SecondaryButton(
                    text = stringResource(R.string.auth_reg_btn),
                    enabled = false,
                    onClick = {})
            }
        }
    }
}
