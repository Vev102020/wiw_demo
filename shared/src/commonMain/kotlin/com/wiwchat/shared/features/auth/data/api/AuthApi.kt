package com.wiwchat.shared.features.auth.data.api

import com.wiwchat.shared.features.auth.data.dto.ForgotPasswordRequest
import com.wiwchat.shared.features.auth.data.dto.LoginRequest
import com.wiwchat.shared.features.auth.data.dto.RegisterRequest
import com.wiwchat.shared.features.auth.data.dto.ResendCodeRequest
import com.wiwchat.shared.features.auth.data.dto.ResetPasswordRequest
import com.wiwchat.shared.features.auth.data.dto.VerifyEmailRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AuthApi @Inject constructor(
    private val httpClient: HttpClient
) {
    private val basePath = "auth"

    suspend fun register(request: RegisterRequest): HttpResponse {
        return httpClient.post("$basePath/register") {
            setJsonBody(request)
        }
    }

    suspend fun verifyEmail(request: VerifyEmailRequest): HttpResponse {
        return httpClient.post("$basePath/verify-email") {
            setJsonBody(request)
        }
    }

    suspend fun resendCode(request: ResendCodeRequest): HttpResponse {
        return httpClient.post("$basePath/resend-verification-code") {
            setJsonBody(request)
        }
    }

    suspend fun login(request: LoginRequest): HttpResponse {
        return httpClient.post("$basePath/login") {
            setJsonBody(request)
        }
    }

    suspend fun forgotPassword(request: ForgotPasswordRequest): HttpResponse {
        return httpClient.post("$basePath/forgot-password") {
            setJsonBody(request)
        }
    }

    suspend fun resetPassword(request: ResetPasswordRequest): HttpResponse {
        return httpClient.post("$basePath/reset-password") {
            setJsonBody(request)
        }
    }
}

private fun HttpRequestBuilder.setJsonBody(body: Any) {
    contentType(ContentType.Application.Json)
    setBody(body)
}