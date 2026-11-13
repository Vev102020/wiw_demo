package com.wiwchat.features.settings.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.wiwchat.features.profile.presentation.screen.SetupProfileRoute
import com.wiwchat.features.profile.presentation.viewmodel.SetupProfileViewModel
import com.wiwchat.features.settings.screen.ChangePasswordRoute
import com.wiwchat.features.settings.viewmodel.ChangePasswordViewModel
import kotlinx.serialization.Serializable

@Serializable
object SettingsGraph

sealed interface SettingsDestinations {
    @Serializable object SetupProfile : SettingsDestinations
    @Serializable object ChangePassword : SettingsDestinations
}

fun NavGraphBuilder.settingsNavGraph(navController: NavHostController) {
    navigation<SettingsGraph>(
        startDestination = SettingsDestinations.ChangePassword
    ) {
        composable<SettingsDestinations.SetupProfile> {
            val viewModel: SetupProfileViewModel = hiltViewModel()
            SetupProfileRoute(viewModel = viewModel)
        }

        composable<SettingsDestinations.ChangePassword> {
            val changePasswordViewModel: ChangePasswordViewModel = hiltViewModel()
            ChangePasswordRoute(
                viewModel = changePasswordViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}