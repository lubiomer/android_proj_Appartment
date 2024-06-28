package com.management.roomates.repository

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.management.roomates.data.dao.BillingDao
import com.management.roomates.data.model.BillingItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingRepository(private val billingDao: BillingDao, private val externalScope: CoroutineScope) {

    private val firestore = FirebaseFirestore.getInstance()
    @Volatile
    private var firestoreListener: ListenerRegistration? = null

    lateinit var allBillings: LiveData<List<BillingItem>>

    init {
        setupFirestoreListener()
    }

    fun getAllBillings(id:String):LiveData<List<BillingItem>>{
        allBillings = billingDao.getAllBillings(id)
        return allBillings
    }

    private fun setupFirestoreListener() {
        if (firestoreListener == null) {
            firestoreListener = firestore.collection("billing_items")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }

                    snapshots?.documentChanges?.forEach { documentChange ->
                        val billingItem = documentChange.document.toObject(BillingItem::class.java)
                        when (documentChange.type) {
                            com.google.firebase.firestore.DocumentChange.Type.ADDED,
                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                insertOrUpdateToRoom(billingItem)
                            }
                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                deleteFromRoom(billingItem)
                            }
                        }
                    }
                }
        }
    }

    private fun insertOrUpdateToRoom(billingItem: BillingItem) {
        externalScope.launch(Dispatchers.IO) {
            val existingItem = billingDao.getBillingByDate(billingItem.date)
            if (existingItem == null) {
                billingDao.insertBilling(billingItem)
            } else {
                billingDao.updateBilling(billingItem)
            }
        }
    }

    private fun deleteFromRoom(billingItem: BillingItem) {
        externalScope.launch(Dispatchers.IO) {
            billingDao.deleteBilling(billingItem)
        }
    }

    suspend fun insert(billingItem: BillingItem) {
        withContext(Dispatchers.IO) {
            // Check if an item with the same date already exists in the local database
            val existingItem = billingDao.getBillingByDate(billingItem.date)

            // If no item exists for the given date, insert into both local database and Firestore
            if (existingItem == null) {
                val itemId = billingDao.insertBilling(billingItem) // Insert into local database to generate ID
                firestore.collection("billing_items").document(itemId.toString()).set(billingItem)
            }
        }
    }


    suspend fun update(billingItem: BillingItem) {
        withContext(Dispatchers.IO) {
            billingDao.updateBilling(billingItem)
            firestore.collection("billing_items").document(billingItem.id.toString()).set(billingItem)
        }
    }

    suspend fun delete(billingItem: BillingItem) {
        withContext(Dispatchers.IO) {
            billingDao.deleteBilling(billingItem)
            firestore.collection("billing_items").document(billingItem.id.toString()).delete()
        }
    }

    fun cleanup() {
        firestoreListener?.remove()
        firestoreListener = null
    }
}
