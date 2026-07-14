package com.example.customercrm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.customercrm.util.CrmApplicationChannel

class CrmApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CrmApplicationChannel.CHANNEL_ID,
                "Follow-up reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to call customers on their scheduled follow-up date"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
