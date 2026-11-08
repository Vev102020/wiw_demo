package features.chat.data.table

import com.wiwchat.shared.core.message.data.dto.enums.MessageType
import features.auth.data.UsersTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object MessagesTable : Table("messages") {
    val roomId = uuid("room_id").references(ChatRoomsTable.id, onDelete = ReferenceOption.CASCADE)
    val messageSequenceId = long("message_sequence_id")
    val senderId = uuid("sender_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val text = text("text").nullable()
    val type = customEnumeration(
        name = "type",
        sql = "VARCHAR(20)",
        fromDb = { MessageType.valueOf(it as String) },
        toDb = { it.name }
    ).default(MessageType.TEXT)

    val isEncrypted = bool("is_encrypted").default(false) // Если TRUE — текст сообщения зашифрован

    // Указывает на message_sequence_id родительского сообщения внутри ТОЙ ЖЕ комнаты.
    // Если NULL — это обычное сообщение в чате. Если заполнено — это комментарий в ветке
    val parentMessageSequenceId = long("parent_message_message_sequence_id").nullable()

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").nullable()
    val deletedAt = timestamp("deleted_at").nullable()
    val expiresAt = timestamp("expires_at").nullable()

    override val primaryKey = PrimaryKey(roomId, messageSequenceId)

    init {
        // Задаем внешний ключ на саму себя!
        // Если родительское сообщение будет полностью удалено — база каскадно затрет и все комментарии под ним
        foreignKey(roomId, parentMessageSequenceId, target = primaryKey, onDelete = ReferenceOption.CASCADE)
    }
}