package features.chat.data.table.tags


import com.wiwchat.shared.core.tags.data.dto.enums.TagColorPalette
import features.auth.data.UsersTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import java.util.UUID

// Таблица Тегов
object MessageTagsTable : Table("message_tags") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 15) // Например: "Работа", "Пароли", "Важное"
    val color = customEnumeration(
        name = "color",
        sql = "VARCHAR(20)",
        fromDb = { TagColorPalette.valueOf(it as String) },
        toDb = { it.name }
    ).default(TagColorPalette.WHITE)

    val isSecret = bool("is_secret").default(false) // Если TRUE — требует спец.код устройства для показа!

    // Запрещает пользователю удалять или переименовывать системный тег "Секретно"
    val isEditable = bool("is_editable").default(true)

    override val primaryKey = PrimaryKey(id)
}

