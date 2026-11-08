package features.chat.data.table

import com.wiwchat.shared.core.user.data.dto.enums.UserOnlineStatus
import com.wiwchat.shared.features.chat.data.dto.enums.ChatRoles
import features.auth.data.UsersTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Участники чата
 */
object ChatParticipantsTable : Table("chat_participants") {
    val roomId = uuid("room_id").references(ChatRoomsTable.id, onDelete = ReferenceOption.CASCADE)
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val joinedAt = timestamp("joined_at").defaultExpression(CurrentTimestamp)

    val roleInChat = customEnumeration(
        name = "role_in_chat",
        sql = "VARCHAR(20)",
        fromDb = { ChatRoles.valueOf(it as String) },
        toDb = {it.name}
    ).default(ChatRoles.MEMBER)

    val isMuted = bool("is_muted").default(false) // Навсегда или нет
    val mutedUntil = timestamp("muted_until").nullable() // До конкретного времени (например, заглушить на 2 часа)

    override val primaryKey = PrimaryKey(roomId, userId)
}