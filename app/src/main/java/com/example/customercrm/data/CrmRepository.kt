package com.example.customercrm.data

import kotlinx.coroutines.flow.Flow

class CrmRepository(
    private val customerDao: CustomerDao,
    private val callLogDao: CallLogDao,
    private val followUpDao: FollowUpDao
) {
    val allCustomers: Flow<List<Customer>> = customerDao.getAll()
    val allFollowUps: Flow<List<FollowUp>> = followUpDao.getAll()

    fun getCustomer(id: Long): Flow<Customer?> = customerDao.getById(id)
    fun getCallLogs(customerId: Long): Flow<List<CallLog>> = callLogDao.getForCustomer(customerId)
    fun getFollowUps(customerId: Long): Flow<List<FollowUp>> = followUpDao.getForCustomer(customerId)

    suspend fun addCustomer(customer: Customer): Long = customerDao.insert(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.update(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.delete(customer)

    suspend fun addCallLog(callLog: CallLog) = callLogDao.insert(callLog)
    suspend fun deleteCallLog(callLog: CallLog) = callLogDao.delete(callLog)

    suspend fun addFollowUp(followUp: FollowUp): Long = followUpDao.insert(followUp)
    suspend fun updateFollowUp(followUp: FollowUp) = followUpDao.update(followUp)
    suspend fun deleteFollowUp(followUp: FollowUp) = followUpDao.delete(followUp)
    suspend fun getFollowUpOnce(id: Long): FollowUp? = followUpDao.getByIdOnce(id)
}
