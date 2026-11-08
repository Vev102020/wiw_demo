package com.wiwchat.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

//    BasicTextField(
//        value = value,
//        onValueChange = onValueChange,
//        modifier = modifier
//            .height(44.dp) // Жестко задаем высоту дизайна
//            .fillMaxWidth(),
//        interactionSource = interactionSource,
//        singleLine = true,
//        textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
//    ) { innerTextField ->
//        TextFieldDefaults.DecorationBox(
//            value = value,
//            innerTextField = innerTextField,
//            enabled = true,
//            singleLine = true,
//            visualTransformation = VisualTransformation.None,
//            interactionSource = interactionSource,
//            // Переопределяем внутренние отступы, чтобы текст не уезжал
//            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
//            placeholder = {
//                Text(text = "Поиск...", fontSize = 16.sp)
//            },
//            leadingIcon = {
//                Icon(
//                    imageVector = Icons.Default.Search,
//                    contentDescription = "Иконка поиска"
//                )
//            },
//            // Задаем контейнер (скругление и цвет)
//            container = {
//                TextFieldDefaults.ContainerBox(
//                    enabled = true,
//                    isError = false,
//                    interactionSource = interactionSource,
//                    colors = TextFieldDefaults.colors(
//                        focusedIndicatorColor = Color.Transparent,    // Убираем нижнюю линию
//                        unfocusedIndicatorColor = Color.Transparent,  // Убираем нижнюю линию
//                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
//                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
//                    ),
//                    shape = RoundedCornerShape(8.dp) // Радиус скругления из дизайна
//                )
//            }
//        )
//    }
}