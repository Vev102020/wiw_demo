package features.chat.data.table

import com.wiwchat.shared.core.message.data.dto.enums.EditMessageSeverity
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

// Таблица изменений сообщений с уровнем серьезности
object MessageHistoryTable : Table("message_history") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val roomId = uuid("room_id")
    val messageSequenceId = long("message_sequence_id")
    val oldText = text("old_text")
    val severity = customEnumeration(
        name = "severity",
        sql = "VARCHAR(20)",
        fromDb = { EditMessageSeverity.valueOf(it as String) },
        toDb = { it.name }
    )
    val editedAt = timestamp("edited_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(roomId, messageSequenceId, target = MessagesTable.primaryKey, onDelete = ReferenceOption.CASCADE)
    }
}