package com.wiwchat.shared.common.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wiwchat.shared.common.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    // Слушаем изменения сессии в реальном времени
    @Query("SELECT * FROM user_session LIMIT 1")
    fun observeSession(): Flow<SessionEntity?>

    // Получаем сессию один раз синхронно
    @Query("SELECT * FROM user_session LIMIT 1")
    suspend fun getSessionFirst(): SessionEntity?

    // Сохраняем сессию при авторизации или обновлении токена
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: SessionEntity)

    // Очищаем таблицу при выходе
    @Query("DELETE FROM user_session")
    suspend fun clearSession()
}