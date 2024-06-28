package com.management.roomates.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.management.roomates.data.model.BillingItem

@Dao
interface BillingDao {
    @Query("SELECT * FROM billing_items WHERE apartmentId=:id")
//    @Query("SELECT * FROM billing_items WHERE apartmentId=:id")
    fun getAllBillings(id:String): LiveData<List<BillingItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBilling(billingItem: BillingItem):Long

    @Delete
    suspend fun deleteBilling(billingItem: BillingItem)

    @Update
    suspend fun updateBilling(billingItem: BillingItem)

    @Query("SELECT * FROM billing_items WHERE date = :date LIMIT 1")
    suspend fun getBillingByDate(date: String): BillingItem?
}
