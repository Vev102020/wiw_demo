package com.wiwchat.core.di

import com.wiwchat.shared.common.data.local.dao.SessionDao
import com.wiwchat.shared.common.data.local.dao.UserDao
import com.wiwchat.shared.common.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.koin.mp.KoinPlatformTools
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(): AppDatabase {
        return KoinPlatformTools.defaultContext().get().get<AppDatabase>()
    }

    @Provides
    @Singleton
    fun provideSessionDao(database: AppDatabase): SessionDao {
        return database.sessionDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}