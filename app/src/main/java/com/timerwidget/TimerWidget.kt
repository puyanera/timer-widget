package com.timerwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.RemoteViews

class TimerWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_START_TIMER) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val seconds = prefs.getInt("duration_$appWidgetId", 120)
                val label = prefs.getString("label_$appWidgetId", formatTime(seconds)) ?: formatTime(seconds)
                val timerIntent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                    putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                    putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                    putExtra(AlarmClock.EXTRA_MESSAGE, label)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(timerIntent)
                    NotificationHelper.showTimerStarted(context, label)
                } catch (e: Exception) {
                    // No clock app available
                }
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        for (id in appWidgetIds) {
            editor.remove("duration_$id")
            editor.remove("label_$id")
        }
        editor.apply()
    }

    companion object {
        const val PREFS_NAME = "TimerWidgetPrefs"
        const val ACTION_START_TIMER = "com.timerwidget.START_TIMER"

        fun formatTime(seconds: Int): String {
            val mins = seconds / 60
            val secs = seconds % 60
            return if (secs == 0) "${mins}m" else "${mins}m ${secs}s"
        }

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val seconds = prefs.getInt("duration_$appWidgetId", 120)
            val label = prefs.getString("label_$appWidgetId", formatTime(seconds)) ?: formatTime(seconds)

            val views = RemoteViews(context.packageName, R.layout.widget_timer)
            views.setTextViewText(R.id.widget_label, label)

            val intent = Intent(context, TimerWidget::class.java).apply {
                action = ACTION_START_TIMER
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
