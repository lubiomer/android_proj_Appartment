package com.management.roomates.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.management.roomates.CustomApplication
import com.management.roomates.data.dao.GroceryDao
import com.management.roomates.data.model.GroceryItem
import com.management.roomates.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GroceryRepository(
    private val groceryDao: GroceryDao,
    private val externalScope: CoroutineScope,
    private val currentUserId: String
) {

    private val firestore = FirebaseFirestore.getInstance()
    private var firestoreListener: ListenerRegistration? = null

    private val _apartmentId = MutableLiveData<String>()
    val apartmentId: LiveData<String> get() = _apartmentId

    val allGroceries: LiveData<List<GroceryItem>> get() = groceryDao.getUserGroceries(CustomApplication.loggedUserApartmentId)

    private val _upcomingPurchases = MutableLiveData<List<GroceryItem>?>()
    val upcomingPurchases: LiveData<List<GroceryItem>?> get() = _upcomingPurchases

    init {
        fetchApartmentId()
        firestoreListener = firestore.collection("grocery_items")
//            .whereEqualTo("lastUpdatedBy", currentUserId)
            .whereEqualTo("apartmentId", CustomApplication.loggedUserApartmentId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                snapshots?.documentChanges?.forEach { documentChange ->
                    val groceryItem = documentChange.document.toObject(GroceryItem::class.java)
                    when (documentChange.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                            syncInsertToRoom(groceryItem)
                        }
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            syncUpdateToRoom(groceryItem)
                        }
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            syncDeleteFromRoom(groceryItem)
                        }
                    }
                }
            }
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


    private fun syncInsertToRoom(groceryItem: GroceryItem) {
        externalScope.launch(Dispatchers.IO) {
            val existingItem = groceryDao.getItemByName(groceryItem.name)
            if (existingItem == null) {
                groceryDao.insert(groceryItem)
            }
        }
    }

    private fun syncUpdateToRoom(groceryItem: GroceryItem) {
        externalScope.launch(Dispatchers.IO) {
            groceryDao.update(groceryItem)
        }
    }

    private fun syncDeleteFromRoom(groceryItem: GroceryItem) {
        externalScope.launch(Dispatchers.IO) {
            groceryDao.delete(groceryItem)
        }
    }

    suspend fun insert(groceryItem: GroceryItem) {
        val itemId = withContext(Dispatchers.IO) {
            groceryDao.insert(groceryItem)
        }
        withContext(Dispatchers.IO) {
            firestore.collection("grocery_items").document(itemId.toString()).set(groceryItem)
        }
    }

    suspend fun update(groceryItem: GroceryItem) {
        withContext(Dispatchers.IO) {
            groceryDao.update(groceryItem)
            firestore.collection("grocery_items").document(groceryItem.id.toString()).set(groceryItem)
        }
    }

    suspend fun delete(groceryItem: GroceryItem) {
        withContext(Dispatchers.IO) {
            groceryDao.delete(groceryItem)
            firestore.collection("grocery_items").document(groceryItem.id.toString()).delete()
        }
    }

    fun setApartmentId(apartmentId: String) {
        _apartmentId.postValue(apartmentId)
        externalScope.launch {
            fetchUpcomingPurchases(apartmentId)
        }
    }

    private fun fetchApartmentId() {
        externalScope.launch(Dispatchers.IO) {
            val user = FirebaseAuth.getInstance().currentUser ?: return@launch
            val userEmail = user.email ?: return@launch

            val apartments = firestore.collection("apartments")
                .get()
                .await()

            for (document in apartments.documents) {
                val roommates = document.get("roommates") as? List<Map<String, Any>> ?: continue
                for (roommate in roommates) {
                    if (roommate["email"] == userEmail) {
                        val apartmentId = document.getString("name") ?: document.id

                        _apartmentId.postValue(apartmentId)

                        fetchUpcomingPurchases(apartmentId)
                        return@launch
                    }
                }
            }
        }
    }

    private fun fetchUpcomingPurchases(apartmentId: String) {
        externalScope.launch(Dispatchers.IO) {
            val currentTime = Date() // Current date and time
            val myFormat = "yyyy-MM-dd" // Specify your format
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            val formattedDate = sdf.format(currentTime) // Format the current date

            val purchases = groceryDao.getUpcomingPurchases(formattedDate, apartmentId).value

            _upcomingPurchases.postValue(purchases)
        }
    }

    fun cleanup() {
        firestoreListener?.remove()
    }
}
