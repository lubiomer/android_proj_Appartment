package com.management.roomates.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.firestore.FirebaseFirestore
import com.management.roomates.data.model.User
import com.management.roomates.repository.UserRepository
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userRepository: UserRepository = UserRepository(firestore)

    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    fun login() = liveData {
        try {
            val user = userRepository.getUserByEmailAndPassword(email.value!!, password.value!!)
            if (user != null) {
                _user.postValue(user!!)
                emit(Result.success(user))
            } else {
                emit(Result.failure(Exception("Invalid email or password")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun register(user: User) = liveData {
        try {
            userRepository.createUser(user)
            _user.postValue(user)
            emit(Result.success(user))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
