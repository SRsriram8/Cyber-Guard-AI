package com.example.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

object FirebaseAuthManager {

    private fun getFirebaseAuth(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signInWithEmailAndPassword(email: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        val auth = getFirebaseAuth()
            ?: return@withContext Result.failure(Exception("Firebase Authentication service unavailable (Default FirebaseApp not initialized)."))

        return@withContext suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email.trim(), pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    val userEmail = user?.email ?: email.trim()
                    if (continuation.isActive) {
                        continuation.resume(Result.success(userEmail))
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(exception))
                    }
                }
        }
    }

    suspend fun createUserWithEmailAndPassword(
        username: String,
        email: String,
        pass: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val auth = getFirebaseAuth()
            ?: return@withContext Result.failure(Exception("Firebase Authentication service unavailable (Default FirebaseApp not initialized)."))

        return@withContext suspendCancellableCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(email.trim(), pass)
                .addOnSuccessListener { result ->
                    val firebaseUser = result.user
                    if (firebaseUser != null && username.isNotBlank()) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(username.trim())
                            .build()
                        firebaseUser.updateProfile(profileUpdates)
                    }
                    val userEmail = firebaseUser?.email ?: email.trim()
                    if (continuation.isActive) {
                        continuation.resume(Result.success(userEmail))
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(exception))
                    }
                }
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        val auth = getFirebaseAuth()
            ?: return@withContext Result.failure(Exception("Firebase Authentication service unavailable (Default FirebaseApp not initialized)."))

        return@withContext suspendCancellableCoroutine { continuation ->
            auth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener {
                    if (continuation.isActive) {
                        continuation.resume(Result.success(Unit))
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(exception))
                    }
                }
        }
    }

    fun signOut() {
        try {
            getFirebaseAuth()?.signOut()
        } catch (_: Exception) { }
    }

    fun getCurrentFirebaseUserEmail(): String? {
        return try {
            getFirebaseAuth()?.currentUser?.email
        } catch (_: Exception) {
            null
        }
    }
}
