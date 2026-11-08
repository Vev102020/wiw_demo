package features.chat.data.table.tags

import features.chat.data.table.MessagesTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

// Связующая таблица Сообщение <-> Тег (У одного сообщения может быть много тегов)
object MessageTagRelationsTable : Table("message_tag_relations") {
    val roomId = uuid("room_id")
    val messageSequenceId = long("message_sequence_id")
    val tagId = uuid("tag_id").references(MessageTagsTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(roomId, messageSequenceId, tagId)

    init {
        // Связываем внешний ключ сразу по двум колонкам с MessagesTable
        foreignKey(roomId, messageSequenceId, target = MessagesTable.primaryKey)
    }
}