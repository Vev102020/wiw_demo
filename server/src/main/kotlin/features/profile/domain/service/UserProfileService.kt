package features.profile.domain.service

import com.wiwchat.shared.core.user.data.dto.enums.UserOnlineStatus
import com.wiwchat.shared.features.profile.data.dto.UserProfileDto
import core.errors.UsernameAlreadyTakenException
import features.auth.data.UsersTable
import features.profile.data.mapper.toUserProfileDto
import features.profile.data.table.UserProfilesTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

class UserProfileService {
    /**
     * Статусы онлайна
     */
    suspend fun setUserOnline(userId: UUID) {
        newSuspendedTransaction(Dispatchers.IO) {
            UserProfilesTable.update({ UserProfilesTable.userId eq userId }) {
                it[this.status] = UserOnlineStatus.ONLINE
                it[this.lastSeen] = Instant.now() // Обновляем время активности на текущее
            }
        }
    }

    suspend fun setUserOffline(userId: UUID): Long {
        val now = Instant.now()
        newSuspendedTransaction(Dispatchers.IO) {
            UserProfilesTable.update({ UserProfilesTable.userId eq userId }) {
                it[this.status] = UserOnlineStatus.OFFLINE
                it[this.lastSeen] = now
            }
        }
        return now.toEpochMilli()
    }

    /**
    * Обновляем имя
    */
    suspend fun updateDisplayName(userId: UUID, displayName: String): UserProfileDto {
        val cleanName = displayName.trim()

        return newSuspendedTransaction(Dispatchers.IO) {
            UserProfilesTable.update({ UserProfilesTable.userId eq userId }) {
                it[this.displayName] = cleanName
            }

            (UsersTable innerJoin UserProfilesTable)
                .selectAll()
                .where { UsersTable.id eq userId }
                .single()
                .toUserProfileDto()
        }
    }

    /**
     * Обновляем username
     */
    suspend fun updateUsername(userId: UUID, username: String): UserProfileDto {
        val cleanUsername = username.trim().lowercase()

        return newSuspendedTransaction(Dispatchers.IO) {
            // Проверяем, не занят ли этот тег другим пользователем
            val isUsernameTaken = UserProfilesTable
                .selectAll()
                .where {
                    (UserProfilesTable.username eq cleanUsername) and (UserProfilesTable.userId neq userId)
                }
                .any()

            if (isUsernameTaken) {
                throw UsernameAlreadyTakenException()
            }

            UserProfilesTable.update({ UserProfilesTable.userId eq userId }) {
                it[this.username] = cleanUsername
            }

            (UsersTable innerJoin UserProfilesTable)
                .selectAll()
                .where { UsersTable.id eq userId }
                .single()
                .toUserProfileDto()
        }
    }
}