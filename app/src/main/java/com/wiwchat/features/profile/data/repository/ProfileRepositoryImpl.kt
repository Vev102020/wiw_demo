package com.wiwchat.features.profile.data.repository

import com.wiwchat.shared.common.data.local.dao.UserDao
import com.wiwchat.shared.common.data.local.entity.UserEntity
import com.wiwchat.shared.common.data.network.util.safeApiCall
import com.wiwchat.shared.common.domain.model.DomainResult
import com.wiwchat.shared.features.profile.data.api.ProfileApi
import com.wiwchat.shared.features.profile.data.dto.UpdateDisplayNameRequest
import com.wiwchat.shared.features.profile.data.dto.UpdateUsernameRequest
import com.wiwchat.shared.features.profile.data.dto.UserProfileDto
import com.wiwchat.shared.features.profile.domain.repository.ProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ProfileRepositoryImpl @Inject constructor(
    private val profileApi: ProfileApi,
    private val userDao: UserDao
) : ProfileRepository {

    override fun observeCurrentUser(): Flow<UserEntity?> = userDao.getUser()

    override suspend fun updateDisplayName(displayName: String): DomainResult<UserProfileDto> {
        return safeApiCall<UserProfileDto> {
            profileApi.updateDisplayName(UpdateDisplayNameRequest(displayName))
        }.also { result ->
            if (result is DomainResult.Success) {
                syncLocalUser(result.value)
            }
        }
    }

    override suspend fun updateUsername(username: String): DomainResult<UserProfileDto> {
        return safeApiCall<UserProfileDto> {
            profileApi.updateUsername(UpdateUsernameRequest(username))
        }.also { result ->
            if (result is DomainResult.Success) {
                syncLocalUser(result.value)
            }
        }
    }

    private suspend fun syncLocalUser(dto: UserProfileDto) {
        val currentUser = userDao.getUserById(dto.id) ?: return
        userDao.insertUser(
            currentUser.copy(
                displayName = dto.displayName,
                username = dto.username
            )
        )
    }
}