package com.example.customercrm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val company: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
