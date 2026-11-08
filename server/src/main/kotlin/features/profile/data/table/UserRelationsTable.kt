package features.profile.data.table

import com.wiwchat.shared.core.user.data.dto.enums.RelationType
import features.auth.data.UsersTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object UserRelationsTable : Table("user_relations") {
    // Тот, кто настраивает приватность
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)

    // Целевой пользователь (спамер / недоброжелатель)
    val targetUserId = uuid("target_user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)

    // Тип ограничения (MUTE_SOUND, HIDE_MESSAGES, GLOBAL_BLOCK)
    val type = customEnumeration(
        name = "relation_type",
        sql = "VARCHAR(25)",
        fromDb = { RelationType.valueOf(it as String) },
        toDb = { it.name }
    )

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    // Составной первичный ключ: одна связь между двумя конкретными людьми
    override val primaryKey = PrimaryKey(userId, targetUserId)
}