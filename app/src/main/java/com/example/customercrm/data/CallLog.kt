package com.example.customercrm.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

enum class CallOutcome { CONNECTED, NO_ANSWER, FOLLOW_UP_NEEDED, DEAL_CLOSED, NOT_INTERESTED }

@Entity(
    tableName = "call_logs",
    foreignKeys = [ForeignKey(
        entity = Customer::class,
        parentColumns = ["id"],
        childColumns = ["customerId"],
        onDelete = androidx.room.ForeignKey.CASCADE
    )]
)
data class CallLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val callDateTime: Long,
    val outcome: CallOutcome,
    val notes: String = ""
)
