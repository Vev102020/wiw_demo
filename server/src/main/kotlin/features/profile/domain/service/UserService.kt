package features.profile.domain.service

import com.wiwchat.shared.core.user.data.dto.enums.UserOnlineStatus
import features.profile.data.table.UserProfilesTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

class UserService {
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
}