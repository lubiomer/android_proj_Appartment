package com.management.roomates.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.management.roomates.data.model.GroceryItem

@Dao
interface GroceryDao {
    @Query("SELECT * FROM grocery_items WHERE apartmentId = :id")
    fun getUserGroceries(id: String): LiveData<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items WHERE date >= :currentDate AND apartmentId = :apartmentId")
    fun getUpcomingPurchases(currentDate: String, apartmentId: String): LiveData<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items ORDER BY lastUpdatedAt DESC LIMIT 1")
    fun getLastUpdated(): LiveData<GroceryItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(groceryItem: GroceryItem):Long

    @Update
    suspend fun update(groceryItem: GroceryItem)

    @Delete
    suspend fun delete(groceryItem: GroceryItem)

    @Query("SELECT * FROM grocery_items WHERE name = :name")
    suspend fun getItemByName(name: String): GroceryItem?
}
