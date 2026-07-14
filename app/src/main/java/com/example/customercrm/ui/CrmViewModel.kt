package com.example.customercrm.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.customercrm.data.*
import com.example.customercrm.util.CalendarHelper
import com.example.customercrm.util.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CrmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CrmRepository
    private val appContext = application.applicationContext

    val allCustomers: StateFlow<List<Customer>>
    val allFollowUps: StateFlow<List<FollowUp>>

    init {
        val db = AppDatabase.getInstance(application)
        repository = CrmRepository(db.customerDao(), db.callLogDao(), db.followUpDao())

        allCustomers = repository.allCustomers.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allFollowUps = repository.allFollowUps.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
    }

    fun getCustomer(id: Long) = repository.getCustomer(id)
    fun getCallLogs(customerId: Long) = repository.getCallLogs(customerId)
    fun getFollowUps(customerId: Long) = repository.getFollowUps(customerId)

    fun addCustomer(name: String, phone: String, company: String, notes: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addCustomer(Customer(name = name.trim(), phone = phone.trim(), company = company.trim(), notes = notes.trim()))
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch { repository.deleteCustomer(customer) }
    }

    fun addCallLog(customerId: Long, dateTime: Long, outcome: CallOutcome, notes: String) {
        viewModelScope.launch {
            repository.addCallLog(CallLog(customerId = customerId, callDateTime = dateTime, outcome = outcome, notes = notes.trim()))
        }
    }

    /**
     * Creates a follow-up reminder: schedules a local notification alarm AND
     * writes an event to the on-device local calendar. Nothing here touches
     * the network or any cloud account.
     */
    fun addFollowUp(customerId: Long, customerName: String, dueDateTime: Long, note: String) {
        viewModelScope.launch {
            val eventId = CalendarHelper.addEvent(
                appContext,
                title = "Call $customerName",
                description = note,
                startMillis = dueDateTime
            )
            val id = repository.addFollowUp(
                FollowUp(customerId = customerId, customerName = customerName, dueDateTime = dueDateTime, note = note.trim(), calendarEventId = eventId)
            )
            ReminderScheduler.schedule(appContext, id, customerName, note, dueDateTime)
        }
    }

    fun markFollowUpDone(followUp: FollowUp) {
        viewModelScope.launch {
            repository.updateFollowUp(followUp.copy(isDone = true))
            ReminderScheduler.cancel(appContext, followUp.id)
        }
    }

    fun deleteFollowUp(followUp: FollowUp) {
        viewModelScope.launch {
            ReminderScheduler.cancel(appContext, followUp.id)
            followUp.calendarEventId?.let { CalendarHelper.deleteEvent(appContext, it) }
            repository.deleteFollowUp(followUp)
        }
    }
}
