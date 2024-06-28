package com.management.roomates.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "cleaning_shifts")
data class CleaningShift(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val roommateName: String,
    val dateToClean: Long,
    val lastUpdatedBy: String,
    val lastUpdatedAt: Long,
    val apartmentId: String
){
    constructor() : this(0, "", 0, "", 0L,"")


}
