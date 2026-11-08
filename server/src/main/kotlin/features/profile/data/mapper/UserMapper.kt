package features.profile.data.mapper

import com.wiwchat.shared.core.user.data.dto.enums.UserOnlineStatus
import com.wiwchat.shared.features.profile.data.dto.UserProfileDto
import features.auth.data.UsersTable
import features.profile.data.table.UserProfilesTable
import features.profile.domain.model.CreatedUserData
import org.jetbrains.exposed.sql.ResultRow


fun ResultRow.toUserProfileDto(): UserProfileDto {
    return UserProfileDto(
        id = this[UsersTable.id].toString(),
        username = this[UserProfilesTable.username],
        displayName = this[UserProfilesTable.displayName],
        avatarUrl = null, // Новый пользователь или только вошедший (загрузится позже)
        status = UserOnlineStatus.OFFLINE.name,
        lastSeen = System.currentTimeMillis()
    )
}

/**
 * Переводит данные только что зарегистрированного пользователя в безопасный DTO
 */
fun CreatedUserData.toUserProfileDto(): UserProfileDto {
    return UserProfileDto(
        id = this.userId.toString(),
        username = this.username,
        displayName = this.displayName,
        avatarUrl = null,
        status = UserOnlineStatus.OFFLINE.name,
        lastSeen = System.currentTimeMillis()
    )
}