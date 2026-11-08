package com.wiwchat.features.auth.presentation.viewmodel

import androidx.lifecycle.*
import com.wiwchat.shared.common.data.local.dao.SessionDao
import com.wiwchat.shared.common.data.network.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*


sealed interface SessionState {
    object Idle : SessionState
    object Unauthenticated : SessionState
    object Authenticated : SessionState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionDao: SessionDao,
    private val webSocketManager: WebSocketManager
) : ViewModel() {
    val sessionState: StateFlow<SessionState> = sessionDao.observeSession()
        .map { sessionEntity ->
            if (sessionEntity != null) {
                SessionState.Authenticated
            } else {
                SessionState.Unauthenticated
            }
        }
        .stateIn(
            scope = viewModelScope,
            // Стрим активен, пока открыт экран (с буфером в 5 сек при поворотах устройства)
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionState.Idle
        )

    init {
        // Запускаем отслеживание сессии для управления web-сокетом
        viewModelScope.launch {
            sessionDao.observeSession().collectLatest { session ->
                if (session != null) {
                    // Пользователь вошел -> подключаем сокет FORCE_LOGOUT от Ktor
                    webSocketManager.connect(session.userId)
                } else {
                    // Разлогинен/Выбит сервером -> выключаем сокет
                    webSocketManager.disconnect()
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionDao.clearSession()
        }
    }
}