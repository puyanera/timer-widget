package com.timerwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.view.View
import android.widget.RemoteViews

class MultiTimerWidget : AppWidgetProvider() {

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
        if (intent.action == ACTION_START_MULTI_TIMER) {
            val seconds = intent.getIntExtra(EXTRA_SECONDS, -1)
            val label = intent.getStringExtra(EXTRA_LABEL) ?: ""
            if (seconds > 0) {
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
            for (slot in 0 until MAX_SLOTS) {
                editor.remove("slot_${id}_${slot}_seconds")
                editor.remove("slot_${id}_${slot}_label")
            }
        }
        editor.apply()
    }

    companion object {
        const val PREFS_NAME = "MultiTimerWidgetPrefs"
        const val ACTION_START_MULTI_TIMER = "com.timerwidget.START_MULTI_TIMER"
        const val EXTRA_SECONDS = "extra_seconds"
        const val EXTRA_LABEL = "extra_label"
        const val MAX_SLOTS = 5

        val SLOT_BUTTON_IDS = intArrayOf(
            R.id.btn_slot_1, R.id.btn_slot_2, R.id.btn_slot_3,
            R.id.btn_slot_4, R.id.btn_slot_5
        )

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val views = RemoteViews(context.packageName, R.layout.widget_multi_timer)

            for (slot in 0 until MAX_SLOTS) {
                val seconds = prefs.getInt("slot_${appWidgetId}_${slot}_seconds", -1)
                val buttonId = SLOT_BUTTON_IDS[slot]

                if (seconds > 0) {
                    val label = prefs.getString(
                        "slot_${appWidgetId}_${slot}_label",
                        TimerWidget.formatTime(seconds)
                    ) ?: TimerWidget.formatTime(seconds)

                    views.setViewVisibility(buttonId, View.VISIBLE)
                    views.setTextViewText(buttonId, label)

                    val intent = Intent(context, MultiTimerWidget::class.java).apply {
                        action = ACTION_START_MULTI_TIMER
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        putExtra(EXTRA_SECONDS, seconds)
                        putExtra(EXTRA_LABEL, label)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        appWidgetId * 100 + slot,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(buttonId, pendingIntent)
                } else {
                    views.setViewVisibility(buttonId, View.GONE)
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
