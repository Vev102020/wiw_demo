package features.voice.data.table

import com.wiwchat.shared.features.voice.data.dto.enums.VoiceRoomType
import features.auth.data.UsersTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object VoiceChannelsTable : Table("voice_channels") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val title = varchar("title", 50)
    val type = customEnumeration(
        name = "type",
        sql = "VARCHAR(20)",
        fromDb = { VoiceRoomType.valueOf(it as String) },
        toDb = { it.name }
    ).default(VoiceRoomType.PERMANENT)

    // Кто создал (для временных комнат, чтобы удалить канал, когда создатель вышел)
    val creatorId = uuid("creator_id")
        .references(UsersTable.id, onDelete = ReferenceOption.CASCADE).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}