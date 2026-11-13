package com.wiwchat

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.wiwchat.features.auth.navigation.*
import com.wiwchat.features.auth.presentation.viewmodel.*
import com.wiwchat.features.main.navigation.*
import com.wiwchat.features.settings.navigation.SettingsDestinations
import com.wiwchat.features.settings.navigation.settingsNavGraph

@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel,
) {
    val navController = rememberNavController()
    val sessionState by authViewModel.sessionState.collectAsState()

    LaunchedEffect(sessionState) {
        when (val state = sessionState) {
            is SessionState.Authenticated -> {
                if (state.isProfileCompleted) {
                    // Имя уже заполнено -> пускаем в основной граф чатов
                    navController.navigate(MainGraph) {
                        popUpTo(0) { inclusive = true }
                    }
                } else {
                    // Имя еще null -> открываем экран онбординга
                    navController.navigate(SettingsDestinations.SetupProfile) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is SessionState.Unauthenticated -> {
                navController.navigate(AuthGraph) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is SessionState.Idle -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = AuthGraph,
    ) {
        authNavGraph(navController)
        mainNavGraph(navController)
        settingsNavGraph(navController)
    }
}
