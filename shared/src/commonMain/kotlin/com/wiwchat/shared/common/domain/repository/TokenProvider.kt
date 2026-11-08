package com.wiwchat.shared.common.domain.repository

interface TokenProvider {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun saveTokens(userId: String, accessToken: String, refreshToken: String, )
    suspend fun clearTokens()
}