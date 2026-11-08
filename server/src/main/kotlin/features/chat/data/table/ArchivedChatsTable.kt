package features.chat.data.table

import features.auth.data.UsersTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object ArchivedChatsTable : Table("archived_chats") {
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val roomId = uuid("room_id").references(ChatRoomsTable.id, onDelete = ReferenceOption.CASCADE)
    val archivedAt = timestamp("archived_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(userId, roomId)
}