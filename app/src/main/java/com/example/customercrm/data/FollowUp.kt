package com.example.customercrm.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "follow_ups",
    foreignKeys = [ForeignKey(
        entity = Customer::class,
        parentColumns = ["id"],
        childColumns = ["customerId"],
        onDelete = androidx.room.ForeignKey.CASCADE
    )]
)
data class FollowUp(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val dueDateTime: Long,
    val note: String = "",
    val isDone: Boolean = false,
    val calendarEventId: Long? = null
)
