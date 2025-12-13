package com.example.taskmasterfinalproject.addtask

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmasterfinalproject.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "extra_task_title"
        const val EXTRA_DESCRIPTION = "extra_task_description"
        const val EXTRA_DUE_DATE = "extra_task_due_date"
        const val EXTRA_PRIORITY = "extra_task_priority"
        const val EXTRA_DUE_TIME_MILLIS = "extra_due_time_millis" // New constant
    }

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var dueDateEditText: EditText
    private lateinit var prioritySpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private var dueTimeInMillis: Long? = null // New member variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        titleEditText = findViewById(R.id.edit_task_title)
        descriptionEditText = findViewById(R.id.edit_task_description)
        dueDateEditText = findViewById(R.id.edit_task_due_date)
        prioritySpinner = findViewById(R.id.spinner_task_priority)
        saveButton = findViewById(R.id.button_save_task)
        cancelButton = findViewById(R.id.button_cancel_task)

        dueDateEditText.setOnClickListener {
            showDateTimePicker()
        }

        setupPrioritySpinner()
        setupButtons()
    }

    private fun setupPrioritySpinner() {
        val priorities = resources.getStringArray(R.array.task_priority_entries)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            priorities
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = adapter
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                // Set the member variable
                this.dueTimeInMillis = calendar.timeInMillis

                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val formatted = formatter.format(calendar.time)
                dueDateEditText.setText(formatted)
            }

            TimePickerDialog(
                this,
                timeListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        DatePickerDialog(
            this,
            dateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupButtons() {
        cancelButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val dueDate = dueDateEditText.text.toString()

            val selectedPosition = prioritySpinner.selectedItemPosition
            val priority = if (selectedPosition in 0..2) selectedPosition + 1 else 0

            val resultIntent = Intent().apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_DESCRIPTION, description)
                putExtra(EXTRA_DUE_DATE, dueDate)
                putExtra(EXTRA_PRIORITY, priority)
                // Add the time in millis using the new key
                putExtra(EXTRA_DUE_TIME_MILLIS, dueTimeInMillis ?: -1L)
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
