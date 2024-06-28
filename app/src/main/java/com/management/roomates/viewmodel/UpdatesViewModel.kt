package com.management.roomates.viewmodel

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.management.roomates.data.model.CleaningShift
import com.management.roomates.data.model.Update
import com.management.roomates.data.model.User
import com.management.roomates.repository.CleaningShiftRepository
import com.management.roomates.repository.UpdatesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UpdatesViewModel(
    private val currentUserId: String
) : ViewModel() {

    private val externalScope = CoroutineScope(Dispatchers.IO)
    private val updatesRepository: UpdatesRepository = UpdatesRepository(externalScope, currentUserId)
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val allUpdates = MutableLiveData<List<Update>>()

    private val _currentUpdates = MutableLiveData<Update?>()
    val currentUpdates: LiveData<Update?> get() = _currentUpdates

    fun loadUpdates(id:String): LiveData<List<Update>> = liveData {
            val updates = firestore.collection("updates")
                .whereEqualTo("apartmentId", id)
                .get().await().toObjects(Update::class.java)
            emit(updates)

    }

    fun insert(update: Update,callback:(message:String)-> Unit) {
        val ref = firestore.collection("updates").document()
        update.id = ref.id
        firestore.collection("updates").document(ref.id).set(update).addOnSuccessListener {
            callback("success")
        }
    }

    fun delete(update: Update,callback: (message: String) -> Unit) {
        firestore.collection("updates").document(update.id).delete()
            .addOnSuccessListener {
                if (update.image.isNotEmpty()){
                    val storage = FirebaseStorage.getInstance()
                    val storageRef = storage.reference
                    val lastPathSegment = update.image.toUri().lastPathSegment
                    val imageRef = storageRef.child("$lastPathSegment")

                    imageRef.delete().addOnSuccessListener {
                        callback("success")
                    }
                }else{
                    callback("success")
                }

            }
    }
}

class UpdatesViewModelFactory(
    private val currentUserId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UpdatesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UpdatesViewModel(currentUserId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
