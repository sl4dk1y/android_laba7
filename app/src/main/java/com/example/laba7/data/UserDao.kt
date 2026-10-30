package com.example.laba7.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT COUNT(*) FROM users") suspend fun count(): Int
    @Query("SELECT * FROM users WHERE login = :login LIMIT 1") suspend fun findByLogin(login: String): User?
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1") suspend fun findById(id: Long): User?
    @Query("SELECT * FROM users ORDER BY id") fun observeAll(): Flow<List<User>>
    @Insert suspend fun insert(user: User): Long
    @Update suspend fun update(user: User)
}
