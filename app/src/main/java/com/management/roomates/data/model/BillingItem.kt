package com.management.roomates.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "billing_items")
data class BillingItem(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val date: String = "",
    val amount: Double = 0.0,
    val status: String = "",
    val lastUpdatedBy: String = "",
    val lastUpdatedAt: Long = 0L,
    val apartmentId: String
) {
    // Firestore requires a no-argument constructor
    constructor() : this(0, "", 0.0, "", "", 0L, "")
}
