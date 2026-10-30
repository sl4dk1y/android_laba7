package com.example.laba7.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "auth_logs",
    indices = [Index(value = ["login", "timestamp"], unique = true)]
)
data class AuthLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long?,
    val login: String,
    val timestamp: Long
)
