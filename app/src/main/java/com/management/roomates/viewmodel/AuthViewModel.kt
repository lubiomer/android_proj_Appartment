package com.management.roomates.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.management.roomates.data.model.User
import com.management.roomates.repository.UserRepository
import kotlinx.coroutines.tasks.await

class AuthViewModel(private val context: Context) : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userRepository: UserRepository = UserRepository(firestore)

    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    fun login() = liveData {
        try {
            val result = auth.signInWithEmailAndPassword(email.value!!, password.value!!).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = userRepository.getUserById(firebaseUser.uid)
                if (user != null) {
                    _user.postValue(user)
                    sharedPreferences.edit().putBoolean("is_logged_in", true).apply()
                    emit(Result.success(user))
                } else {
                    emit(Result.failure(Exception("User not found in Firestore")))
                }
            } else {
                emit(Result.failure(Exception("Authentication failed")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun register(user: User) = liveData {
        try {
            val result = auth.createUserWithEmailAndPassword(user.email, user.password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                user.id = firebaseUser.uid
                userRepository.createUser(user)
                _user.postValue(user)
                emit(Result.success(user))
            } else {
                emit(Result.failure(Exception("Registration failed")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun logout() {
        auth.signOut()
        sharedPreferences.edit().putBoolean("is_logged_in", false).apply()
    }
}
