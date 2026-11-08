package com.wiwchat.features.auth.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.wiwchat.features.auth.presentation.screen.WelcomeScreen
import com.wiwchat.features.auth.presentation.viewmodel.LoginViewModel
import kotlinx.serialization.Serializable
import com.wiwchat.features.auth.presentation.screen.*
import com.wiwchat.features.auth.presentation.viewmodel.*

@Serializable
object AuthGraph

sealed interface AuthDestinations {
    @Serializable object Welcome : AuthDestinations
    @Serializable object Login : AuthDestinations
    @Serializable object Register : AuthDestinations
    @Serializable object Forgot : AuthDestinations

    @Serializable
    data class Verification(
        val email: String,
        val token: String? = null
    ) : AuthDestinations

    @Serializable
    data class ResetPassword(
        val email: String,
        val token: String
    ) : AuthDestinations
}

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation<AuthGraph>(
        startDestination = AuthDestinations.Welcome
    ) {
        composable<AuthDestinations.Welcome> {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate(AuthDestinations.Login) },
                onNavigateToRegister = { navController.navigate(AuthDestinations.Register) }
            )
        }

        composable<AuthDestinations.Login> {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginRoute(
                viewModel = loginViewModel,
                onNavigateToRegister = { navController.navigate(AuthDestinations.Register) },
                onNavigateToForgot = { navController.navigate(AuthDestinations.Forgot) },
                onNavigateToVerification = { email ->
                    navController.navigate(AuthDestinations.Verification(email = email))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AuthDestinations.Register> {
            val registerViewModel: RegisterViewModel = hiltViewModel()
            RegisterRoute(
                viewModel = registerViewModel,
                onNavigateToLogin = { navController.navigate(AuthDestinations.Login) },
                onNavigateToVerification = { email, token ->
                    navController.navigate(AuthDestinations.Verification(email = email, token = token))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AuthDestinations.Verification> {
            val verificationViewModel: VerificationViewModel = hiltViewModel()
            VerificationRoute(
                viewModel = verificationViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AuthDestinations.Forgot> {
            val forgotViewModel: ForgotViewModel = hiltViewModel()
            ForgotRoute(
                viewModel = forgotViewModel,
                onNavigateToResetVerification = { email, resetToken ->
                    navController.navigate(AuthDestinations.ResetPassword(email = email, token = resetToken))
                },
                onNavigateToLogin = { navController.navigate(AuthDestinations.Login) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AuthDestinations.ResetPassword> {
            val resetViewModel: ResetPasswordViewModel = hiltViewModel()
            ResetPasswordRoute(
                viewModel = resetViewModel,
                onNavigateToLogin = {
                    navController.navigate(AuthDestinations.Login) {
                        popUpTo<AuthDestinations.Forgot> { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
