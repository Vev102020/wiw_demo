package com.wiwchat.shared.common.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.wiwchat.shared.common.data.local.dao.SessionDao
import com.wiwchat.shared.common.data.local.dao.UserDao
import com.wiwchat.shared.common.data.local.database.AppDatabase
import com.wiwchat.shared.common.data.local.impl.TokenProviderImpl
import com.wiwchat.shared.common.data.network.WebSocketManager
import com.wiwchat.shared.common.domain.repository.TokenProvider
import com.wiwchat.shared.features.auth.data.api.AuthApi
import com.wiwchat.shared.features.auth.domain.usecase.LoginUseCase
import org.koin.dsl.module
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.http.Url
import com.wiwchat.shared.core.config.AppEnvironment
import com.wiwchat.shared.features.auth.data.dto.TokenResponse
import com.wiwchat.shared.features.auth.domain.repository.AuthRepository
import com.wiwchat.shared.features.auth.domain.usecase.ResetPasswordUseCase
import com.wiwchat.shared.features.auth.domain.usecase.VerifyEmailUseCase
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.encodedPath


// Модуль сетевого слоя
val networkModule = module {
    single<HttpClient> {
        val rawUrl: String = AppEnvironment.baseUrl
        val parsedUrl = Url(rawUrl)
        val basePath =  "/api/v1/"

        HttpClient {
            defaultRequest {
                url {
                    protocol = parsedUrl.protocol
                    host = parsedUrl.host
                    port = if (parsedUrl.port > 0) parsedUrl.port else protocol.defaultPort
                    encodedPath = basePath
                }
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }

            install(WebSockets)

            install(Auth) {
                bearer {
                    // Загрузка токенов для обычных запросов
                    loadTokens {
                        val provider = get<TokenProvider>()
                        val access = provider.getAccessToken()
                        val refresh = provider.getRefreshToken()
                        if (access != null && refresh != null) {
                            BearerTokens(access, refresh)
                        } else null
                    }
                    // Автоматическое обновление токенов при 401 ошибке
                    refreshTokens {
                        val provider = get<TokenProvider>()
                        val refreshToken = provider.getRefreshToken() ?: return@refreshTokens null

                        try {
                            // Создаем отдельный чистый клиент для запроса обновления, чтобы избежать рекурсии
                            val refreshClient = HttpClient {
                                install(ContentNegotiation) { json() }
                            }

                            // Делаем запрос к серверу на обновление
                            val response = refreshClient.post {
                                url {
                                    protocol = parsedUrl.protocol
                                    host = parsedUrl.host
                                    port = parsedUrl.port
                                    path("$basePath/auth/refresh")
                                }
                                contentType(ContentType.Application.Json)
                                setBody(refreshToken)
                            }.body<TokenResponse>()

                            // Сохраняем новые токены через мультиплатформенный провайдер
                            provider.saveTokens(
                                userId = response.userId,
                                accessToken = response.accessToken,
                                refreshToken = response.refreshToken
                            )

                            BearerTokens(response.accessToken, response.refreshToken)
                        } catch (e: Exception) {
                            provider.clearTokens()
                            null
                        }
                    }
                }
            }

        }
    }

    single { AuthApi(get()) }

    single<WebSocketManager> {
        val rawUrl: String = AppEnvironment.baseUrl
        val parsedUrl = Url(rawUrl)
        WebSocketManager(
            client = get(),
            tokenProvider = get(),
            serverUrl = parsedUrl.host
        )
    }

    single<TokenProvider> { TokenProviderImpl(sessionDao = get()) }
}

// Модуль репозиториев
val repositoryModule = module {}

// Модуль бизнес-логики
val useCaseModule = module {
    factory { LoginUseCase(get<AuthRepository>() ) }
    factory { ResetPasswordUseCase(get<AuthRepository>()) }
    factory { VerifyEmailUseCase(get<AuthRepository>()) }
}

val sharedDatabaseModule = module {
    single<AppDatabase> {
        val builder: RoomDatabase.Builder<AppDatabase> = get()
        builder
            .setDriver(BundledSQLiteDriver())
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    single<SessionDao> {
        get<AppDatabase>().sessionDao()
    }

    single<UserDao> {
        get<AppDatabase>().userDao()
    }
}

// Общий список всех модулей shared-слоя
val sharedModules = listOf(
    sharedDatabaseModule,
    networkModule,
    repositoryModule,
    useCaseModule
)