package com.example.customercrm.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.customercrm.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Android clears scheduled alarms on reboot. This receiver re-schedules
 * any pending (not-yet-due, not-done) follow-up reminders after restart.
 * Purely local — reads from the on-device Room database only.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(appContext)
            val list = db.followUpDao().getAll().first()
            val now = System.currentTimeMillis()
            list.filter { !it.isDone && it.dueDateTime > now }
                .forEach {
                    ReminderScheduler.schedule(appContext, it.id, it.customerName, it.note, it.dueDateTime)
                }
        }
    }
}
