package com.example.data.remote

import android.util.Log
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FirestoreSyncManager {

    private const val TAG = "FirestoreSyncManager"

    private fun getFirestore(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore instance not available: ${e.localizedMessage}")
            null
        }
    }

    private fun sanitizeEmailKey(email: String): String {
        return email.trim().lowercase().replace(".", "_dot_")
    }

    suspend fun saveSessionToCloud(userEmail: String, session: ChatSessionEntity) = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) return@withContext
        val db = getFirestore() ?: return@withContext
        try {
            val userKey = sanitizeEmailKey(userEmail)
            val sessionMap = hashMapOf(
                "id" to session.id,
                "title" to session.title,
                "createdTimestamp" to session.createdTimestamp,
                "updatedTimestamp" to session.updatedTimestamp,
                "isPinned" to session.isPinned,
                "isFavorite" to session.isFavorite
            )
            db.collection("users")
                .document(userKey)
                .collection("sessions")
                .document(session.id)
                .set(sessionMap, SetOptions.merge())
                .await()
            Log.d(TAG, "Session ${session.id} synced to Firestore for $userEmail")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save session to Firestore: ${e.localizedMessage}")
        }
    }

    suspend fun saveMessageToCloud(userEmail: String, message: ChatMessageEntity) = withContext(Dispatchers.IO) {
        if (userEmail.isBlank() || message.sessionId.isBlank()) return@withContext
        val db = getFirestore() ?: return@withContext
        try {
            val userKey = sanitizeEmailKey(userEmail)
            val msgId = if (message.id != 0L) message.id.toString() else "${message.timestamp}_${message.role}"
            val messageMap = hashMapOf(
                "id" to message.id,
                "sessionId" to message.sessionId,
                "role" to message.role,
                "content" to message.content,
                "timestamp" to message.timestamp,
                "toolType" to message.toolType,
                "codeSnippet" to message.codeSnippet
            )
            db.collection("users")
                .document(userKey)
                .collection("sessions")
                .document(message.sessionId)
                .collection("messages")
                .document(msgId)
                .set(messageMap, SetOptions.merge())
                .await()
            Log.d(TAG, "Message $msgId synced to Firestore for session ${message.sessionId}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save message to Firestore: ${e.localizedMessage}")
        }
    }

    suspend fun deleteSessionFromCloud(userEmail: String, sessionId: String) = withContext(Dispatchers.IO) {
        if (userEmail.isBlank() || sessionId.isBlank()) return@withContext
        val db = getFirestore() ?: return@withContext
        try {
            val userKey = sanitizeEmailKey(userEmail)
            val sessionRef = db.collection("users")
                .document(userKey)
                .collection("sessions")
                .document(sessionId)

            // Delete subcollection messages
            val messages = sessionRef.collection("messages").get().await()
            for (doc in messages.documents) {
                doc.reference.delete().await()
            }
            sessionRef.delete().await()
            Log.d(TAG, "Session $sessionId deleted from Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete session from Firestore: ${e.localizedMessage}")
        }
    }

    suspend fun syncCloudToLocal(userEmail: String, chatDao: ChatDao): Result<Int> = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) return@withContext Result.failure(Exception("No user logged in"))
        val db = getFirestore()
            ?: return@withContext Result.failure(Exception("Firestore service unavailable"))

        try {
            val userKey = sanitizeEmailKey(userEmail)
            val sessionDocs = db.collection("users")
                .document(userKey)
                .collection("sessions")
                .get()
                .await()

            var syncedCount = 0
            for (doc in sessionDocs.documents) {
                val sId = doc.getString("id") ?: doc.id
                val title = doc.getString("title") ?: "Synced Chat"
                val created = doc.getLong("createdTimestamp") ?: System.currentTimeMillis()
                val updated = doc.getLong("updatedTimestamp") ?: System.currentTimeMillis()
                val isPinned = doc.getBoolean("isPinned") ?: false
                val isFavorite = doc.getBoolean("isFavorite") ?: false

                val sessionEntity = ChatSessionEntity(
                    id = sId,
                    title = title,
                    createdTimestamp = created,
                    updatedTimestamp = updated,
                    isPinned = isPinned,
                    isFavorite = isFavorite
                )
                chatDao.insertSession(sessionEntity)

                // Fetch messages for this session
                val messageDocs = doc.reference.collection("messages").get().await()
                for (mDoc in messageDocs.documents) {
                    val role = mDoc.getString("role") ?: "assistant"
                    val content = mDoc.getString("content") ?: ""
                    val timestamp = mDoc.getLong("timestamp") ?: System.currentTimeMillis()
                    val toolType = mDoc.getString("toolType") ?: "GENERAL"
                    val codeSnippet = mDoc.getString("codeSnippet")

                    val msgEntity = ChatMessageEntity(
                        sessionId = sId,
                        role = role,
                        content = content,
                        timestamp = timestamp,
                        toolType = toolType,
                        codeSnippet = codeSnippet
                    )
                    chatDao.insertMessage(msgEntity)
                }
                syncedCount++
            }
            Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing Firestore to local DB: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun syncLocalToCloud(userEmail: String, chatDao: ChatDao) = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) return@withContext
        try {
            val sessions = chatDao.getSessionsList()
            for (session in sessions) {
                saveSessionToCloud(userEmail, session)
                val messages = chatDao.getMessagesListForSession(session.id)
                for (msg in messages) {
                    saveMessageToCloud(userEmail, msg)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing local chats to cloud: ${e.localizedMessage}")
        }
    }
}
