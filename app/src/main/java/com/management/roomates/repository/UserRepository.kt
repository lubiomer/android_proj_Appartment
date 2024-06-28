package com.management.roomates.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.management.roomates.data.model.User
import kotlinx.coroutines.tasks.await

class UserRepository(private val firestore: FirebaseFirestore) {
    private val usersCollection = firestore.collection("users")

    suspend fun getUserById(id: String): User? {
        val document = usersCollection.document(id).get().await()
        return if (document.exists()) {
            document.toObject(User::class.java)
        } else {
            null
        }
    }

    suspend fun createUser(user: User) {
        usersCollection.document(user.id).set(user).await()
    }
}
