package com.wiwchat.shared.features.auth.data.api

import com.wiwchat.shared.common.data.network.util.setJsonBody
import com.wiwchat.shared.features.auth.data.dto.*
import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.post
import jakarta.inject.*

@Singleton
class AuthApi @Inject constructor(
    private val httpClient: HttpClient
) {
    // не писать / в начале, чтобы не сбить url
    private val authBasePath = "auth"

    suspend fun register(request: RegisterRequest): HttpResponse {
        return httpClient.post("$authBasePath/register") {
            setJsonBody(request)
        }
    }

    suspend fun verifyOtp(request: VerifyOtpRequest): HttpResponse {
        return httpClient.post("$authBasePath/verify-otp") {
            setJsonBody(request)
        }
    }

    suspend fun resendOtp(request: ResendOtpRequest): HttpResponse {
        return httpClient.post("$authBasePath/resend-otp") {
            setJsonBody(request)
        }
    }

    suspend fun login(request: LoginRequest): HttpResponse {
        return httpClient.post("$authBasePath/login") {
            setJsonBody(request)
        }
    }

    suspend fun forgotPassword(request: ForgotPasswordRequest): HttpResponse {
        return httpClient.post("$authBasePath/password/forgot") {
            setJsonBody(request)
        }
    }

    suspend fun verifyResetOtp(request: VerifyResetOtpRequest): HttpResponse {
        return httpClient.post("$authBasePath/password/verify-otp") {
            setJsonBody(request)
        }
    }
    suspend fun resetPassword(request: ResetPasswordRequest): HttpResponse {
        return httpClient.post("$authBasePath/password/reset") {
            setJsonBody(request)
        }
    }

    suspend fun changePassword(request: ChangePasswordRequest): HttpResponse {
        return httpClient.post("$authBasePath/password/change") {
            setJsonBody(request)
        }
    }

    suspend fun logout(request: RefreshRequest): HttpResponse {
        return httpClient.post("$authBasePath/logout") {
            setJsonBody(request)
        }
    }
}
