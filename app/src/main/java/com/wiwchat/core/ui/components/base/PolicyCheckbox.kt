package com.wiwchat.core.ui.components.base

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import com.wiwchat.app.R
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.wiwchat.core.ui.theme.AppTheme
import com.wiwchat.core.ui.theme.appColors
import com.wiwchat.core.ui.theme.spacing

@Composable
fun PolicyCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {

        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            RoundCheckbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.indent))

        val agreeText = stringResource(R.string.cm_policy_agree)
        val linkText = stringResource(R.string.cm_policy_link)

        val annotatedString = buildAnnotatedString {
            append(agreeText)

            withStyle(style = SpanStyle(
                color = MaterialTheme.appColors.linkDefault,
            )
            ) {
                append(" ")
                append(linkText)
            }
        }

        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.appColors.textSecondary,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.clickable(

                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onCheckedChange(!checked)
            }
        )
    }
}

@Preview(showBackground = true, locale = "en")
@Composable
private fun PolicyCheckboxPreview(){
    AppTheme {
        Surface(
            modifier = Modifier.wrapContentWidth()
                .padding(MaterialTheme.spacing.medium),
            color = MaterialTheme.colorScheme.background,
        ) {
            PolicyCheckbox(
                checked = true,
                onCheckedChange = { },
                modifier = Modifier
            )
        }
    }
}