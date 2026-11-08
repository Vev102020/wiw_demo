package com.wiwchat.shared.common.data.local.impl

import com.wiwchat.shared.common.data.local.dao.SessionDao
import com.wiwchat.shared.common.data.local.entity.SessionEntity
import com.wiwchat.shared.common.domain.repository.TokenProvider
import jakarta.inject.Inject

class TokenProviderImpl @Inject constructor(
    private val sessionDao: SessionDao
) : TokenProvider {

    override suspend fun getAccessToken(): String? {
        return sessionDao.getSessionFirst()?.accessToken
    }

    override suspend fun getRefreshToken(): String? {
        return sessionDao.getSessionFirst()?.refreshToken
    }

    override suspend fun saveTokens(userId: String, accessToken: String, refreshToken: String) {
        // очищаем сессию
        sessionDao.clearSession()

        val entity = SessionEntity(
            userId = userId,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
        sessionDao.saveSession(entity)
    }

    override suspend fun clearTokens() {
        sessionDao.clearSession()
    }
}