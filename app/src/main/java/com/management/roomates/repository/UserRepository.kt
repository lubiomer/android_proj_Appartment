package com.management.roomates.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.management.roomates.data.model.User
import kotlinx.coroutines.tasks.await

class UserRepository(private val firestore: FirebaseFirestore) {

    suspend fun createUser(user: User) {
        firestore.collection("users").document(user.email).set(user).await()
    }

    suspend fun getUserByEmailAndPassword(email: String, password: String): User? {
        val document = firestore.collection("users").document(email).get().await()
        val user = document.toObject(User::class.java)
        return if (user?.password == password) user else null
    }

    suspend fun getUser(uid: String): User? {
        val document = firestore.collection("users").document(uid).get().await()
        return document.toObject(User::class.java)
    }

    suspend fun updateUser(user: User) {
        firestore.collection("users").document(user.email).set(user).await()
    }
}
