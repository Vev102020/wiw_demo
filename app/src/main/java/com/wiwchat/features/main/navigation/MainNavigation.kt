package com.wiwchat.features.main.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import kotlinx.serialization.Serializable
import androidx.compose.ui.*

@Serializable
object MainGraph

sealed interface MainDestinations {
    @Serializable object Chats : MainDestinations
    @Serializable object Profile : MainDestinations
}

fun NavGraphBuilder.mainNavGraph(navController: NavHostController) {
    navigation<MainGraph>(
        startDestination = MainDestinations.Chats
    ) {
        composable<MainDestinations.Chats> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chats Screen (Authenticated)")
            }
        }

        composable<MainDestinations.Profile> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Profile Screen")
            }
        }
    }
}