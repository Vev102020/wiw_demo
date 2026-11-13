package com.wiwchat.shared.core.forms

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

class FormFieldState<E>(
    private val scope: CoroutineScope,
    private val maxLength: Int,
    private val validator: (String) -> E?
) {
    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _error = MutableStateFlow<E?>(null)
    val error: StateFlow<E?> = _error.asStateFlow()

    private val _externalError = MutableStateFlow<E?>(null)
    private var wasFocused = false

    // Полноценный поток валидности для кнопки (без учета фокуса)
    val isValid: StateFlow<Boolean> = combine(_text, _externalError) { currentText, externalError ->
        validator(currentText) == null && externalError == null
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun onTextChanged(value: String) {
        _text.value = if (value.length <= maxLength) value else value.take(maxLength)
        _externalError.value = null // Сбрасываем серверную ошибку при вводе нового символа

        // Если пользователь исправляет ошибку и поле стало валидным — гасим визуальную ошибку мгновенно
        if (validator(_text.value) == null) {
            _error.value = null
        }
    }

    fun onFocusChanged(isFocused: Boolean) {
        if (isFocused) {
            wasFocused = true
        } else if (wasFocused) {
            _error.value = validator(_text.value)
        }
    }

    fun validate(): Boolean {
        val err = validator(_text.value)
        _error.value = err
        return err == null
    }

    fun setErrorFromBackend(errorCode: E) {
        _externalError.value = errorCode
        _error.value = errorCode
    }
}