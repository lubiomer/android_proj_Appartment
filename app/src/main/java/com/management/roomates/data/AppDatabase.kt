package com.management.roomates.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.management.roomates.data.dao.BillingDao
import com.management.roomates.data.dao.CleaningShiftDao
import com.management.roomates.data.dao.GroceryDao
import com.management.roomates.data.dao.PostDao
import com.management.roomates.data.model.BillingItem
import com.management.roomates.data.model.CleaningShift
import com.management.roomates.data.model.GroceryItem
import com.management.roomates.data.model.Post

@Database(entities = [Post::class, GroceryItem::class, BillingItem::class, CleaningShift::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun groceryDao(): GroceryDao
    abstract fun billingDao(): BillingDao
    abstract fun cleaningShiftDao(): CleaningShiftDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "roomates_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
