package com.timerwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MultiTimerConfigActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val selectedDurations = mutableListOf<Int>()

    data class PresetEntry(val seconds: Int, val buttonId: Int)

    private val presets = listOf(
        PresetEntry(60,   R.id.btn_p_1m),
        PresetEntry(120,  R.id.btn_p_2m),
        PresetEntry(180,  R.id.btn_p_3m),
        PresetEntry(300,  R.id.btn_p_5m),
        PresetEntry(420,  R.id.btn_p_7m),
        PresetEntry(600,  R.id.btn_p_10m),
        PresetEntry(900,  R.id.btn_p_15m),
        PresetEntry(1200, R.id.btn_p_20m),
        PresetEntry(1500, R.id.btn_p_25m),
        PresetEntry(1800, R.id.btn_p_30m)
    )

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

        setContentView(R.layout.activity_multi_config)
        setupPresetButtons()
        setupCustomInput()
        setupSaveButton()
        refreshPresetButtonStates()
    }

    private fun setupPresetButtons() {
        for (preset in presets) {
            val btn = findViewById<Button>(preset.buttonId)
            btn.setOnClickListener { togglePreset(preset.seconds) }
        }
    }

    private fun togglePreset(seconds: Int) {
        if (selectedDurations.contains(seconds)) {
            selectedDurations.remove(seconds)
        } else if (selectedDurations.size < MultiTimerWidget.MAX_SLOTS) {
            selectedDurations.add(seconds)
        } else {
            Toast.makeText(this, "Maximum ${MultiTimerWidget.MAX_SLOTS} timers", Toast.LENGTH_SHORT).show()
        }
        refreshPresetButtonStates()
        updateSelectedDisplay()
    }

    private fun refreshPresetButtonStates() {
        for (preset in presets) {
            val btn = findViewById<Button>(preset.buttonId)
            if (selectedDurations.contains(preset.seconds)) {
                btn.setBackgroundColor(0xFFFF6B35.toInt())
                btn.setTextColor(0xFFFFFFFF.toInt())
            } else {
                btn.setBackgroundColor(0xFFEEEEEE.toInt())
                btn.setTextColor(0xFF444444.toInt())
            }
        }
    }

    private fun setupCustomInput() {
        val editText = findViewById<EditText>(R.id.custom_minutes)
        val addBtn = findViewById<Button>(R.id.btn_add_custom)
        addBtn.setOnClickListener {
            val mins = editText.text.toString().toIntOrNull()
            when {
                mins == null || mins <= 0 ->
                    Toast.makeText(this, "Enter a valid number of minutes", Toast.LENGTH_SHORT).show()
                selectedDurations.size >= MultiTimerWidget.MAX_SLOTS ->
                    Toast.makeText(this, "Maximum ${MultiTimerWidget.MAX_SLOTS} timers", Toast.LENGTH_SHORT).show()
                else -> {
                    val seconds = mins * 60
                    if (!selectedDurations.contains(seconds)) {
                        selectedDurations.add(seconds)
                        refreshPresetButtonStates()
                        updateSelectedDisplay()
                    }
                    editText.text.clear()
                }
            }
        }
    }

    private fun updateSelectedDisplay() {
        val container = findViewById<LinearLayout>(R.id.selected_container)
        container.removeAllViews()
        for (seconds in selectedDurations.toList()) {
            val label = TimerWidget.formatTime(seconds)
            val btn = Button(this).apply {
                text = "✕ $label"
                setBackgroundColor(0xFF444444.toInt())
                setTextColor(0xFFFFFFFF.toInt())
                setOnClickListener {
                    selectedDurations.remove(seconds)
                    refreshPresetButtonStates()
                    updateSelectedDisplay()
                }
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 8, 0) }
            container.addView(btn, params)
        }
        val countLabel = findViewById<TextView>(R.id.selected_count)
        countLabel.text = "${selectedDurations.size}/${MultiTimerWidget.MAX_SLOTS} selected"
    }

    private fun setupSaveButton() {
        findViewById<Button>(R.id.btn_save).setOnClickListener {
            if (selectedDurations.isEmpty()) {
                Toast.makeText(this, "Please select at least one duration", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val prefs = getSharedPreferences(MultiTimerWidget.PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            for (slot in 0 until MultiTimerWidget.MAX_SLOTS) {
                if (slot < selectedDurations.size) {
                    val seconds = selectedDurations[slot]
                    editor.putInt("slot_${appWidgetId}_${slot}_seconds", seconds)
                    editor.putString("slot_${appWidgetId}_${slot}_label", TimerWidget.formatTime(seconds))
                } else {
                    editor.putInt("slot_${appWidgetId}_${slot}_seconds", -1)
                    editor.remove("slot_${appWidgetId}_${slot}_label")
                }
            }
            editor.apply()
            val appWidgetManager = AppWidgetManager.getInstance(this)
            MultiTimerWidget.updateWidget(this, appWidgetManager, appWidgetId)
            val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}
