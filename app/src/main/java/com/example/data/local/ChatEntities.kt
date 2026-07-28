package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val updatedTimestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolType: String = "GENERAL",
    val codeSnippet: String? = null
)

@Entity(tableName = "saved_reports")
data class SavedReportEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String, // "MALWARE", "CODE_AUDIT", "THREAT_HUNTING", "INCIDENT_REPORT", "SCRIPT"
    val summary: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val snippet: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_progress")
data class QuizProgressEntity(
    @PrimaryKey val topicId: String,
    val topicTitle: String,
    val completedCount: Int,
    val totalCount: Int,
    val scorePercentage: Int,
    val lastStudiedTimestamp: Long = System.currentTimeMillis()
)
