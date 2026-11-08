package com.wiwchat

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.wiwchat.features.auth.navigation.*
import com.wiwchat.features.auth.presentation.viewmodel.*
import com.wiwchat.features.main.navigation.*


@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel,
) {
    val navController = rememberNavController()
    val sessionState by authViewModel.sessionState.collectAsState()

    // Реактивный менеджер сессий: управляет переходами между графами без утечек памяти
    LaunchedEffect(sessionState) {
        when (sessionState) {
            is SessionState.Authenticated -> {
                navController.navigate(MainGraph) {
                    // Очищаем весь AuthGraph из стэка, делая его инклюзивным
                    popUpTo(AuthGraph) { inclusive = true }
                }
            }
            is SessionState.Unauthenticated -> {
                navController.navigate(AuthGraph) {
                    popUpTo(MainGraph) { inclusive = true }
                }
            }
            is SessionState.Idle -> {
                // Ожидание загрузки состояния сессии из локальной БД
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AuthGraph,
    ) {
        authNavGraph(navController)
        mainNavGraph(navController)
    }
}
