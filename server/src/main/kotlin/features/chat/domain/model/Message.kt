package features.chat.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class Message(
    @Contextual val id: UUID,
    @Contextual val chatId: UUID,
    @Contextual val senderId: UUID?,
    val content: String,
    val type: String,
    val fileUrl: String?,
    @Contextual val replyTo: UUID?,
    val isDeleted: Boolean,
    @Contextual val createdAt: LocalDateTime,
    @Contextual val editedAt: LocalDateTime?
)