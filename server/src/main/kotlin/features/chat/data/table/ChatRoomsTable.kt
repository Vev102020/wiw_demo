package features.chat.data.table

import com.wiwchat.shared.features.chat.data.dto.enums.ChatType
import features.auth.data.UsersTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object ChatRoomsTable : Table("chat_rooms") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val type = customEnumeration(
        name = "type",
        sql ="VARCHAR(20)",
        fromDb = { ChatType.valueOf(it as String) },
        toDb = { it.name }
    ).default(ChatType.DIRECT)

    val title = varchar("title", 150).nullable()
    val avatarUrl = text("avatar_url").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val ownerId = uuid("owner_id").references(UsersTable.id).nullable()

    override val primaryKey = PrimaryKey(id)
}