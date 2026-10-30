package com.example.laba7.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["login"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val login: String,
    val passwordHash: String,
    val fullName: String,
    val birthDate: String,
    val gender: String,
    val avatarUri: String?,
    val isAdmin: Boolean
)
