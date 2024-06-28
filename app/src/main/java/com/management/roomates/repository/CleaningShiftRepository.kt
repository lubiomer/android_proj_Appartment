package com.management.roomates.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.management.roomates.CustomApplication
import com.management.roomates.data.dao.CleaningShiftDao
import com.management.roomates.data.model.CleaningShift
import com.management.roomates.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CleaningShiftRepository(
    private val cleaningShiftDao: CleaningShiftDao,
    private val externalScope: CoroutineScope,
    private val currentUserId: String
) {

    private val firestore = FirebaseFirestore.getInstance()
    @Volatile
    private var firestoreListener: ListenerRegistration? = null

    val allCleaningShifts: LiveData<List<CleaningShift>> = cleaningShiftDao.getAllCleaningShifts(CustomApplication.loggedUserApartmentId)


    init {
        setupFirestoreListener()
    }

    fun getUsersByApartmentId(apartmentId: String, callback: (List<User>) -> Unit) {

        firestore.collection("users")
            .whereEqualTo("apartmentId", apartmentId)
            .get()
            .addOnSuccessListener { result ->
                val userList = mutableListOf<User>()
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    userList.add(user)
                }
                callback(userList)
            }
            .addOnFailureListener { exception ->
                // Handle the error
                Log.w("FirestoreError", "Error getting documents: ", exception)
            }
    }

    private fun setupFirestoreListener() {
        if (firestoreListener == null) {
            firestoreListener = firestore.collection("cleaning_shifts")
                .whereEqualTo("apartmentId",CustomApplication.loggedUserApartmentId)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }

                    snapshots?.documentChanges?.forEach { documentChange ->
                        val cleaningShift = documentChange.document.toObject(CleaningShift::class.java)
                        when (documentChange.type) {
                            com.google.firebase.firestore.DocumentChange.Type.ADDED,
                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                insertOrUpdateToRoom(cleaningShift)
                            }
                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                deleteFromRoom(cleaningShift)
                            }
                        }
                    }
                }
        }
    }

    private fun insertOrUpdateToRoom(cleaningShift: CleaningShift) {
        externalScope.launch(Dispatchers.IO) {
            val existingShift = cleaningShiftDao.getCleaningShiftByDate(cleaningShift.lastUpdatedAt)
            if (existingShift == null) {
                cleaningShiftDao.insert(cleaningShift)
            } else {
                cleaningShiftDao.update(cleaningShift)
            }
        }
    }

    private fun deleteFromRoom(cleaningShift: CleaningShift) {
        externalScope.launch(Dispatchers.IO) {
            cleaningShiftDao.delete(cleaningShift)
        }
    }

    suspend fun insert(cleaningShift: CleaningShift) {
        withContext(Dispatchers.IO) {
            // Check if a shift with the same last updated date already exists in the local database
            val existingShift = cleaningShiftDao.getCleaningShiftByDate(cleaningShift.lastUpdatedAt)

            // If no shift exists for the given last updated date, insert into both local database and Firestore
            if (existingShift == null) {
                val shiftId = cleaningShiftDao.insert(cleaningShift) // Insert into local database to generate ID
//                val ref = firestore.collection("cleaning_shifts").document()

                firestore.collection("cleaning_shifts").document(shiftId.toString()).set(cleaningShift)
            }
        }
    }


    suspend fun update(cleaningShift: CleaningShift) {
        withContext(Dispatchers.IO) {
            cleaningShiftDao.update(cleaningShift)
            firestore.collection("cleaning_shifts").document(cleaningShift.id.toString()).set(cleaningShift)
        }
    }

    suspend fun delete(cleaningShift: CleaningShift) {
        withContext(Dispatchers.IO) {
            cleaningShiftDao.delete(cleaningShift)
            firestore.collection("cleaning_shifts").document(cleaningShift.id.toString()).delete()
        }
    }

    fun cleanup() {
        firestoreListener?.remove()
        firestoreListener = null
    }
}
