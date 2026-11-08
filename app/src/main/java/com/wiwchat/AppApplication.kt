package com.wiwchat

import android.app.Application
import androidx.room.RoomDatabase
import com.wiwchat.app.R
import com.wiwchat.shared.common.data.local.database.AppDatabase
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.wiwchat.shared.common.di.sharedModules
import com.wiwchat.shared.common.domain.repository.TokenProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.koin.dsl.module

@HiltAndroidApp
class WiwChatApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val androidDatabaseModule = module {
            single<RoomDatabase.Builder<AppDatabase>> {
                val dbFile = androidContext().getDatabasePath("wiwchat_local.db")
                androidx.room.Room.databaseBuilder<AppDatabase>(
                    context = androidContext(),
                    name = dbFile.absolutePath
                )
            }
        }

        startKoin {
            androidContext(this@WiwChatApp)

//            printLogger(org.koin.core.logger.Level.ERROR)

            modules(androidDatabaseModule + sharedModules)
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface KoinBridgeEntryPoint {
        fun tokenProvider(): TokenProvider
    }
}