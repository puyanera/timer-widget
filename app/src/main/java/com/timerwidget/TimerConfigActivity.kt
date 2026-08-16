package com.timerwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TimerConfigActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContentView(R.layout.activity_config)
        requestNotificationPermission()

        findViewById<Button>(R.id.btn_1min).setOnClickListener  { saveWidget(60) }
        findViewById<Button>(R.id.btn_2min).setOnClickListener  { saveWidget(120) }
        findViewById<Button>(R.id.btn_5min).setOnClickListener  { saveWidget(300) }
        findViewById<Button>(R.id.btn_10min).setOnClickListener { saveWidget(600) }
        findViewById<Button>(R.id.btn_15min).setOnClickListener { saveWidget(900) }
        findViewById<Button>(R.id.btn_20min).setOnClickListener { saveWidget(1200) }
        findViewById<Button>(R.id.btn_30min).setOnClickListener { saveWidget(1800) }

        findViewById<Button>(R.id.btn_save_custom).setOnClickListener {
            val input = findViewById<EditText>(R.id.custom_minutes).text.toString()
            val mins = input.toIntOrNull()
            if (mins == null || mins <= 0) {
                Toast.makeText(this, "Please enter a valid number of minutes", Toast.LENGTH_SHORT).show()
            } else {
                saveWidget(mins * 60)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }

    private fun saveWidget(seconds: Int) {
        val prefs = getSharedPreferences(TimerWidget.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("duration_$appWidgetId", seconds)
            .putString("label_$appWidgetId", TimerWidget.formatTime(seconds))
            .apply()

        val appWidgetManager = AppWidgetManager.getInstance(this)
        TimerWidget.updateWidget(this, appWidgetManager, appWidgetId)

        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, result)
        finish()
    }
}
