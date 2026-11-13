package com.wiwchat.shared.features.profile.domain.repository

import com.wiwchat.shared.common.data.local.entity.UserEntity
import com.wiwchat.shared.common.domain.model.DomainResult
import com.wiwchat.shared.features.profile.data.dto.UserProfileDto
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeCurrentUser(): Flow<UserEntity?>
    suspend fun updateDisplayName(displayName: String): DomainResult<UserProfileDto>
    suspend fun updateUsername(username: String): DomainResult<UserProfileDto>
}