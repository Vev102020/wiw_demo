package features.auth.domain.model

import com.wiwchat.shared.core.user.data.dto.enums.AccountStatus
import java.time.Instant
import java.util.UUID


data class AuthUser(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val role: String,
    val accountStatus: AccountStatus,
    val emailVerifiedAt: Instant?,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val profileStatus: String,
    val lastSeen: Instant
)