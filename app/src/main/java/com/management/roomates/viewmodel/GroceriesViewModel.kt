package com.management.roomates.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.management.roomates.data.AppDatabase
import com.management.roomates.data.model.GroceryItem
import com.management.roomates.data.model.User
import com.management.roomates.repository.GroceryRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class GroceriesViewModel(context: Context, private val currentUserId: String) : ViewModel() {
    private val repository: GroceryRepository

    init {
        val groceryDao = AppDatabase.getDatabase(context).groceryDao()
        repository = GroceryRepository(groceryDao, viewModelScope, currentUserId)
    }

    val allGroceries: LiveData<List<GroceryItem>> = repository.allGroceries
    val upcomingPurchases: LiveData<List<GroceryItem>?> = repository.upcomingPurchases
    val apartmentId: LiveData<String> get() = repository.apartmentId

    fun insert(groceryItem: GroceryItem) {
        viewModelScope.launch {
            repository.insert(groceryItem)
        }
    }

    fun getUsersByApartmentId(apartmentId: String, callback: (List<User>) -> Unit) {
        repository.getUsersByApartmentId(apartmentId,callback)
    }

    fun update(groceryItem: GroceryItem) {
        viewModelScope.launch {
            repository.update(groceryItem)
        }
    }

    fun delete(groceryItem: GroceryItem) {
        viewModelScope.launch {
            repository.delete(groceryItem)
        }
    }

    fun setApartmentId(apartmentId: String) {
        repository.setApartmentId(apartmentId)
    }

    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
    }
}

class GroceriesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val currentUser = FirebaseAuth.getInstance().currentUser
            ?: throw IllegalStateException("User must be logged in")
        val userEmail = currentUser.email ?: "Unknown"
        if (modelClass.isAssignableFrom(GroceriesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroceriesViewModel(context, userEmail) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    } 
}
