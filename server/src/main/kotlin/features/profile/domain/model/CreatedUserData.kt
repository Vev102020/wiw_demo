package features.profile.domain.model

import com.wiwchat.shared.core.user.data.dto.enums.UserRole
import java.util.UUID

data class CreatedUserData(
    val userId: UUID,
    val role: UserRole,
    val username: String,
    val displayName: String
)