package com.wiwchat.shared.common.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_session")
data class SessionEntity(
    @PrimaryKey val userId: String,
    val accessToken: String,
    val refreshToken: String
)