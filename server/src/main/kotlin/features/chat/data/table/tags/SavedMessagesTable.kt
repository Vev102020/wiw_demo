package features.chat.data.table.tags

import features.auth.data.UsersTable
import features.chat.data.table.MessagesTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

// Таблица Избранного (Закладки/Сохраненки)
// Позволяет помечать чужие сообщения из любых чатов и собирать их в одном месте
object SavedMessagesTable : Table("saved_messages") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val originalRoomId = uuid("original_room_id")
    val originalMessageSequenceId = long("original_message_sequence_id")

    val savedAt = timestamp("saved_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)

    init {
        // Устанавливаем составной внешний ключ строго на primaryKey таблицы MessagesTable
        foreignKey(
            originalRoomId,
            originalMessageSequenceId,
            target = MessagesTable.primaryKey,
            onDelete = ReferenceOption.CASCADE
        )
    }
}