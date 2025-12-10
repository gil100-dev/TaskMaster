package com.example.taskmasterfinalproject.settings

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmasterfinalproject.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var remindBeforeSpinner: Spinner
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        remindBeforeSpinner = findViewById(R.id.spinner_remind_before)
        saveButton = findViewById(R.id.button_save_settings)

        setupSpinner()
        loadCurrentValue()

        saveButton.setOnClickListener {
            val selectedMinutes = getSelectedMinutes()
            ReminderPreferences.setRemindBeforeMinutes(this, selectedMinutes)
            Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinner() {
        val entries = listOf(
            Pair(0, getString(R.string.remind_before_at_time)),
            Pair(5, getString(R.string.remind_before_5m)),
            Pair(10, getString(R.string.remind_before_10m)),
            Pair(30, getString(R.string.remind_before_30m)),
            Pair(60, getString(R.string.remind_before_60m))
        )
        val labels = entries.map { it.second }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            labels
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        remindBeforeSpinner.adapter = adapter
        remindBeforeSpinner.tag = entries.map { it.first }
    }

    private fun loadCurrentValue() {
        val entries = remindBeforeSpinner.tag as? List<Int> ?: return
        val current = ReminderPreferences.getRemindBeforeMinutes(this)
        val index = entries.indexOf(current).takeIf { it >= 0 } ?: 0
        remindBeforeSpinner.setSelection(index)
    }

    private fun getSelectedMinutes(): Int {
        val entries = remindBeforeSpinner.tag as? List<Int> ?: return 0
        val position = remindBeforeSpinner.selectedItemPosition
        return entries.getOrNull(position) ?: 0
    }
}
