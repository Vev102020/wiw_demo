package features.auth.data

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object UserSessionsTable : Table("user_sessions") {
    val id = long("id").autoIncrement()
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val refreshToken = text("refresh_token").uniqueIndex()
    val isBanned = bool("is_banned").default(false)
    val deviceInfo = text("device_info").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}
