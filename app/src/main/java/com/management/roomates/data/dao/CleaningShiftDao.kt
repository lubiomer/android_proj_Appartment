package com.management.roomates.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.management.roomates.data.model.CleaningShift

@Dao
interface CleaningShiftDao {
    @Query("SELECT * FROM cleaning_shifts WHERE apartmentId=:id")
    fun getAllCleaningShifts(id:String): LiveData<List<CleaningShift>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cleaningShift: CleaningShift):Long

    @Delete
    suspend fun delete(cleaningShift: CleaningShift)

    @Update
    suspend fun update(cleaningShift: CleaningShift)

    @Query("SELECT * FROM cleaning_shifts WHERE lastUpdatedAt = :date LIMIT 1")
    suspend fun getCleaningShiftByDate(date: Long): CleaningShift?
}
