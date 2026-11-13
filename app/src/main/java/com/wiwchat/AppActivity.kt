package com.wiwchat

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.*
import androidx.appcompat.app.AppCompatActivity
import com.wiwchat.core.ui.components.notification.MainRootContainer
import com.wiwchat.features.auth.presentation.viewmodel.AuthViewModel
import com.wiwchat.core.ui.theme.AppTheme
import com.wiwchat.shared.core.notification.NotificationManager
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class AppActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    @Inject
    lateinit var notificationManager: NotificationManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                MainRootContainer(
                    notificationManager = notificationManager,
                    content = {
                        AppNavGraph(
                            authViewModel = authViewModel
                        )
                    }
                )
            }
        }
    }
}