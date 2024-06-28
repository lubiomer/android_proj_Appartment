package com.management.roomates.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_items")
data class GroceryItem(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val date: String = "",
    var name: String = "",
    var quantity: Int = 0,
    var lastUpdatedBy: String = "",
    var lastUpdatedAt: Long = 0L,
    val apartmentId: String,
    val assignedTo:String
) {
    constructor() : this(0, "","", 0, "", 0L,"","")
}
