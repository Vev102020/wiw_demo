package com.wiwchat.core.di

import com.wiwchat.shared.common.data.network.WebSocketManager
import com.wiwchat.shared.features.auth.data.api.AuthApi
import com.wiwchat.shared.common.domain.repository.TokenProvider
import com.wiwchat.shared.core.notification.NotificationManager
import com.wiwchat.shared.features.profile.data.api.ProfileApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import org.koin.mp.KoinPlatformTools

@Module
@InstallIn(SingletonComponent::class)
object KoinBridgeModule {

    @Provides
    @Singleton
    fun provideTokenProvider(): TokenProvider {
        return KoinPlatformTools.defaultContext().get().get<TokenProvider>()
    }

    @Provides
    @Singleton
    fun provideAuthApi(): AuthApi {
        return KoinPlatformTools.defaultContext().get().get<AuthApi>()
    }

    @Provides
    @Singleton
    fun provideProfileApi(): ProfileApi {
        return KoinPlatformTools.defaultContext().get().get<ProfileApi>()
    }

    @Provides
    @Singleton
    fun provideWebSocketManager(): WebSocketManager {
        return KoinPlatformTools.defaultContext().get().get<WebSocketManager>()
    }

    @Provides
    @Singleton
    fun provideNotificationManager(): NotificationManager{
        return KoinPlatformTools.defaultContext().get().get<NotificationManager>()
    }
}