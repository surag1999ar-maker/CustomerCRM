package com.example.customercrm.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs WHERE customerId = :customerId ORDER BY callDateTime DESC")
    fun getForCustomer(customerId: Long): Flow<List<CallLog>>

    @Query("SELECT * FROM call_logs ORDER BY callDateTime DESC")
    fun getAll(): Flow<List<CallLog>>

    @Insert
    suspend fun insert(callLog: CallLog)

    @Delete
    suspend fun delete(callLog: CallLog)
}
