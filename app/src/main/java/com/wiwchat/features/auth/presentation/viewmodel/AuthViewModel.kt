package com.wiwchat.features.auth.presentation.viewmodel

import androidx.lifecycle.*
import com.wiwchat.shared.common.data.local.dao.SessionDao
import com.wiwchat.shared.common.data.local.dao.UserDao
import com.wiwchat.shared.common.data.network.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*


sealed interface SessionState {
    data object Idle : SessionState
    data object Unauthenticated : SessionState
    data class Authenticated(val isProfileCompleted: Boolean) : SessionState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionDao: SessionDao,
    private val userDao: UserDao,
    private val webSocketManager: WebSocketManager
) : ViewModel() {
    val sessionState: StateFlow<SessionState>  = combine(
        sessionDao.observeSession(),
        userDao.getUser()
    ){ sessionEntity, userEntity ->
        when {
            sessionEntity == null -> SessionState.Unauthenticated
            userEntity == null -> SessionState.Idle
            else -> {
                val isCompleted = !userEntity.displayName.isNullOrBlank()
                SessionState.Authenticated(isProfileCompleted = isCompleted)
            }
        }
    }.stateIn(
        scope = viewModelScope,
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
            userDao.clearUser()
        }
    }
}