package features.auth.data.mapper

import com.wiwchat.shared.features.profile.data.dto.UserProfileDto
import features.auth.data.UsersTable
import features.auth.domain.model.AuthUser
import features.profile.data.table.UserProfilesTable
import org.jetbrains.exposed.sql.ResultRow

fun AuthUser.toUserProfileDto(): UserProfileDto = UserProfileDto(
    id = this.id.toString(),
    username = this.username,
    displayName = this.displayName,
    avatarUrl = this.avatarUrl,
    status = this.profileStatus,
    lastSeen = this.lastSeen.toEpochMilli()
)

fun ResultRow.toAuthUser(): AuthUser {
    return AuthUser(
        id = this[UsersTable.id],
        email = this[UsersTable.email],
        passwordHash = this[UsersTable.passwordHash],
        role = this[UsersTable.role].name,
        accountStatus = this[UsersTable.accountStatus],
        emailVerifiedAt = this[UsersTable.emailVerifiedAt],
        username = this[UserProfilesTable.username],
        displayName = this[UserProfilesTable.displayName],
        avatarUrl = this[UserProfilesTable.avatarUrl],
        profileStatus = this[UserProfilesTable.status].name,
        lastSeen = this[UserProfilesTable.lastSeen]
    )
}