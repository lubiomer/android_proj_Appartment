package com.management.roomates.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.management.roomates.data.model.Task
import com.management.roomates.data.model.Update
import com.management.roomates.data.model.User
import com.management.roomates.repository.UserRepository
import kotlinx.coroutines.tasks.await

class ApartmentManagementViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userRepository: UserRepository = UserRepository(firestore)

    fun loadUserDetails(): LiveData<User> = liveData {
        val currentUser = auth.currentUser
        currentUser?.let {
            val user = userRepository.getUser(it.uid)
            emit(user!!)
        }
    }

    fun loadTasks(): LiveData<List<Task>> = liveData {
        val currentUser = auth.currentUser
        currentUser?.let {
            val tasks = firestore.collection("tasks")
                .whereEqualTo("assignedTo", it.uid)
                .get().await().toObjects(Task::class.java)
            emit(tasks)
        }
    }

    fun loadUpdates(): LiveData<List<Update>> = liveData {
        val currentUser = auth.currentUser
        currentUser?.let {
            val updates = firestore.collection("updates")
                .whereEqualTo("userId", it.uid)
                .get().await().toObjects(Update::class.java)
            emit(updates)
        }
    }
}
