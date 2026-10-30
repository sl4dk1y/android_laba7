package com.example.laba7.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthLogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: AuthLog): Long

    @Query("SELECT * FROM auth_logs ORDER BY timestamp DESC, id DESC")
    fun observeAllNewestFirst(): Flow<List<AuthLog>>
}
