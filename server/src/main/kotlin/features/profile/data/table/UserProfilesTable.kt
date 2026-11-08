package features.profile.data.table

import features.auth.data.UsersTable
import com.wiwchat.shared.core.user.data.dto.enums.UserOnlineStatus
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object UserProfilesTable : Table("user_profiles") {
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val username = varchar("username", 50).uniqueIndex()
    val displayName = varchar("display_name", 100).nullable()
    val avatarUrl = text("avatar_url").nullable()
    val customStatusText = varchar("custom_status_text", 150).nullable()
    val status = customEnumeration(
        name = "status",
        sql = "VARCHAR(20)",
        fromDb = { UserOnlineStatus.valueOf(it as String) },
        toDb = { it.name }
    ).default(UserOnlineStatus.OFFLINE)

    val currentVoiceChannelId = uuid("current_voice_channel_id").nullable() // ID комнаты, где сидит юзер
    val isMuted = bool("is_muted").default(false)       // Замучен ли админом
    val isDeafened = bool("is_deafened").default(false) // Сам выключил себе звук

    val lastSeen = timestamp("last_seen").defaultExpression(CurrentTimestamp)
    val settings = text("settings").default("{}")

    override val primaryKey = PrimaryKey(userId)
}