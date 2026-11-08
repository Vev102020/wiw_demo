package com.wiwchat.core.di

import com.wiwchat.shared.common.data.local.impl.TokenProviderImpl
import com.wiwchat.shared.common.data.network.WebSocketManager
import com.wiwchat.shared.features.auth.data.api.AuthApi
import com.wiwchat.shared.common.domain.repository.TokenProvider
import dagger.Binds
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
    fun provideWebSocketManager(): WebSocketManager {
        return KoinPlatformTools.defaultContext().get().get<WebSocketManager>()
    }
}