package features.auth.data

import com.wiwchat.shared.core.user.data.dto.enums.UserRole
import com.wiwchat.shared.core.user.data.dto.enums.AccountStatus
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object UsersTable : Table("users") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = customEnumeration(
        name= "role",
        sql = "VARCHAR(20)",
        fromDb = { UserRole.valueOf(it as String)},
        toDb = { it.name }
        ).default(UserRole.USER)

    val accountStatus = customEnumeration(
        name = "account_status",
        sql = "VARCHAR(30)",
        fromDb = { AccountStatus.valueOf(it as String)},
        toDb = { it.name }
        ).default(AccountStatus.PENDING_VERIFICATION)

    val emailVerifiedAt = timestamp("email_verified_at").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val lastLoginAt = timestamp("last_login_at").nullable()
    val deletedAt = timestamp("deleted_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
