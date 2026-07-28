package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserEntity(
    @PrimaryKey val email: String,
    val username: String,
    val passwordHash: String,
    val role: String = "Security Analyst",
    val organization: String = "Global Defense SOC",
    val createdTimestamp: Long = System.currentTimeMillis(),
    val lastLoginTimestamp: Long = System.currentTimeMillis()
)
