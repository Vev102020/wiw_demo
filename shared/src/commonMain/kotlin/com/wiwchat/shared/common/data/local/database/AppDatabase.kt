package com.wiwchat.shared.common.data.local.database


import androidx.room.Database
import androidx.room.RoomDatabase
import com.wiwchat.shared.common.data.local.dao.SessionDao
import com.wiwchat.shared.common.data.local.dao.UserDao
import com.wiwchat.shared.common.data.local.entity.SessionEntity
import com.wiwchat.shared.common.data.local.entity.UserEntity


@Database(
    entities = [
        SessionEntity::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun userDao(): UserDao
}