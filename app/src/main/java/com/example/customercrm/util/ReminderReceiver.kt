package com.example.customercrm.util

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.customercrm.MainActivity
import com.example.customercrm.R

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_FOLLOW_UP_ID = "extra_follow_up_id"
        const val EXTRA_CUSTOMER_NAME = "extra_customer_name"
        const val EXTRA_NOTE = "extra_note"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val followUpId = intent.getLongExtra(EXTRA_FOLLOW_UP_ID, 0L)
        val customerName = intent.getStringExtra(EXTRA_CUSTOMER_NAME) ?: "Customer"
        val note = intent.getStringExtra(EXTRA_NOTE) ?: ""

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            followUpId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CrmApplicationChannel.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Follow up: $customerName")
            .setContentText(note.ifBlank { "Time to call this customer" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(followUpId.toInt(), builder.build())
        }
    }
}
