package com.management.roomates.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.management.roomates.CustomApplication
import com.management.roomates.data.model.CleaningShift
import com.management.roomates.data.model.GroceryItem
import com.management.roomates.data.model.Task
import com.management.roomates.data.model.Update
import com.management.roomates.data.model.User
import com.management.roomates.repository.UserRepository
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApartmentManagementViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userRepository: UserRepository = UserRepository(firestore)

    fun loadUserDetails(): LiveData<User> = liveData {
        val currentUser = auth.currentUser
        currentUser?.let {
            val user = userRepository.getUserById(it.uid)
            emit(user!!)
        }
    }

    fun loadGrocery(): LiveData<List<GroceryItem>> = liveData {
        val currentUser = auth.currentUser
//        currentUser?.let {
//            val tasks = firestore.collection("tasks")
//                .whereEqualTo("assignedTo", it.uid)
//                .get().await().toObjects(Task::class.java)
//            emit(tasks)
//        }
        currentUser?.let {
            val tasks = firestore.collection("grocery_items")
                .whereEqualTo("apartmentId",CustomApplication.loggedUserApartmentId)
                .get().await().toObjects(GroceryItem::class.java)
            emit(tasks)
        }
    }

    fun convertTimestampToDateTimeString(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun loadCleaning(): LiveData<List<CleaningShift>> = liveData {
        val currentUser = auth.currentUser
//        currentUser?.let {
//            val tasks = firestore.collection("tasks")
//                .whereEqualTo("assignedTo", it.uid)
//                .get().await().toObjects(Task::class.java)
//            emit(tasks)
//        }
        currentUser?.let {
            val tasks = firestore.collection("cleaning_shifts")
                .whereEqualTo("apartmentId", CustomApplication.loggedUserApartmentId)
                .get().await().toObjects(CleaningShift::class.java)
            emit(tasks)
        }
    }

    fun loadUpdates(apartmentId:String): LiveData<List<Update>> = liveData {
//        val currentUser = auth.currentUser
//        currentUser?.let {
            val updates = firestore.collection("updates")
                .whereEqualTo("apartmentId", apartmentId)
                .get().await().toObjects(Update::class.java)
            emit(updates)
//        }
    }
}
