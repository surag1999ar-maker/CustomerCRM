package com.example.customercrm.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowUpDao {
    @Query("SELECT * FROM follow_ups WHERE customerId = :customerId ORDER BY dueDateTime ASC")
    fun getForCustomer(customerId: Long): Flow<List<FollowUp>>

    @Query("SELECT * FROM follow_ups ORDER BY isDone ASC, dueDateTime ASC")
    fun getAll(): Flow<List<FollowUp>>

    @Insert
    suspend fun insert(followUp: FollowUp): Long

    @Update
    suspend fun update(followUp: FollowUp)

    @Delete
    suspend fun delete(followUp: FollowUp)

    @Query("SELECT * FROM follow_ups WHERE id = :id")
    suspend fun getByIdOnce(id: Long): FollowUp?
}
