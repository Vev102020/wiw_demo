package features.profile.domain.model

import com.wiwchat.shared.core.user.data.dto.enums.UserOnlineStatus
import java.util.UUID

data class UserProfile(
    val userId: UUID,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val onlineStatus: UserOnlineStatus = UserOnlineStatus.OFFLINE,
    val currentVoiceChannelId: String?,
    val isMuted: Boolean = false,
    val isDeafened: Boolean = false,
    val customStatusText: String? = null
)