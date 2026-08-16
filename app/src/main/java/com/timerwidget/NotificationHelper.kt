package com.timerwidget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.concurrent.atomic.AtomicInteger

object NotificationHelper {
    private const val CHANNEL_ID = "timer_started"
    private val notifId = AtomicInteger(0)

    fun showTimerStarted(context: Context, label: String) {
        createChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("$label timer started")
            .setAutoCancel(true)
            .setTimeoutAfter(3000)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(notifId.incrementAndGet(), notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS not granted on Android 13+ — user needs to allow in Settings
        }
    }

    private fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Timer Started",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Brief confirmation when a timer is started from the widget"
            setSound(null, null)
            enableVibration(false)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
