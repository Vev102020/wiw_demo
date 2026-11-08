package com.wiwchat

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.rememberNavController
import com.wiwchat.features.auth.presentation.viewmodel.AuthViewModel
import com.wiwchat.core.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                val navController = rememberNavController()

                AppNavGraph(
                    authViewModel = authViewModel
                )
            }
        }
    }
}