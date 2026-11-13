package com.wiwchat.shared.features.profile.data.api

import com.wiwchat.shared.common.data.network.util.setJsonBody
import com.wiwchat.shared.features.profile.data.dto.UpdateDisplayNameRequest
import com.wiwchat.shared.features.profile.data.dto.UpdateUsernameRequest
import io.ktor.client.request.post
import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import jakarta.inject.Inject
import jakarta.inject.Singleton


@Singleton
class ProfileApi @Inject constructor(
    private val httpClient: HttpClient
){

    // не писать / в начале, чтобы не сбить url
    private val profileBasePath = "users/me"

    suspend fun updateDisplayName(request: UpdateDisplayNameRequest): HttpResponse {
        return httpClient.post("$profileBasePath/display-name") {
            setJsonBody(request)
        }
    }

    suspend fun updateUsername(request: UpdateUsernameRequest): HttpResponse {
        return httpClient.post("$profileBasePath/username") {
            setJsonBody(request)
        }
    }
}