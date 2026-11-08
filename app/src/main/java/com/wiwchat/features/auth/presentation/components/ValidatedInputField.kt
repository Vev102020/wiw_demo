package com.wiwchat.features.auth.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.wiwchat.features.auth.presentation.state.InputFieldState
import com.wiwchat.core.ui.components.InputField

@Composable
fun ValidatedInputField(
    fieldState: InputFieldState,
    label: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        InputField(
            value = fieldState.text.value,
            onValueChange = { fieldState.onTextChanged(it) },
            label = label,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { fieldState.onFocusChanged(it.isFocused) },
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            trailingIcon = trailingIcon
        )
        // Точечный вывод локальной ошибки под полем
        fieldState.errorResId.value?.let { resId ->
            Text(
                text = stringResource(id = resId),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}