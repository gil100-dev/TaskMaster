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
import com.example.taskmasterfinalproject.settings.ReminderPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "extra_task_title"
        const val EXTRA_DESCRIPTION = "extra_task_description"
        const val EXTRA_DUE_DATE = "extra_task_due_date"
        const val EXTRA_PRIORITY = "extra_task_priority"
        const val EXTRA_DUE_TIME_MILLIS = "extra_task_due_time_millis"
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_MODE_ADD = "add"
        const val EXTRA_MODE_EDIT = "edit"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_IS_EDIT = "extra_is_edit"
    }

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var dueDateEditText: EditText
    private lateinit var prioritySpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private var selectedDueTimeMillis: Long? = null
    private var originalTaskId: String? = null
    private var originalDueTimeMillis: Long? = null
    private var mode: String = EXTRA_MODE_ADD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        titleEditText = findViewById(R.id.edit_task_title)
        descriptionEditText = findViewById(R.id.edit_task_description)
        dueDateEditText = findViewById(R.id.edit_task_due_date)
        prioritySpinner = findViewById(R.id.spinner_task_priority)
        saveButton = findViewById(R.id.button_save_task)
        cancelButton = findViewById(R.id.button_cancel_task)

        parseIntent()
        dueDateEditText.setOnClickListener {
            showDateTimePicker()
        }

        setupPrioritySpinner()
        prefillIfEditing()
        setupButtons()
    }

    private fun parseIntent() {
        mode = intent.getStringExtra(EXTRA_MODE) ?: EXTRA_MODE_ADD
        originalTaskId = intent.getStringExtra(EXTRA_TASK_ID)
        originalDueTimeMillis = intent.getLongExtra(EXTRA_DUE_TIME_MILLIS, -1L).takeIf { it >= 0 }
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
        if (originalDueTimeMillis != null) {
            calendar.timeInMillis = originalDueTimeMillis!!
        }

        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                val actualTimeMillis = calendar.timeInMillis
                val leadMinutes = ReminderPreferences.getRemindBeforeMinutes(this)
                val offsetMillis = leadMinutes * 60_000L
                var triggerMillis = actualTimeMillis - offsetMillis
                if (triggerMillis < System.currentTimeMillis()) {
                    triggerMillis = actualTimeMillis
                }
                selectedDueTimeMillis = triggerMillis

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

    private fun prefillIfEditing() {
        if (mode != EXTRA_MODE_EDIT) return
        titleEditText.setText(intent.getStringExtra(EXTRA_TITLE).orEmpty())
        descriptionEditText.setText(intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty())
        dueDateEditText.setText(intent.getStringExtra(EXTRA_DUE_DATE).orEmpty())

        val priority = intent.getIntExtra(EXTRA_PRIORITY, 0)
        val position = if (priority in 1..3) priority - 1 else 0
        prioritySpinner.setSelection(position)

        val dueMillis = originalDueTimeMillis
        if (dueMillis != null && dueMillis >= 0) {
            selectedDueTimeMillis = dueMillis
        }
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
            // Positions are 0-based; priorities are 1, 2, 3. Default to 0 if none.
            val priority = if (selectedPosition in 0..2) selectedPosition + 1 else 0
            val isEdit = mode == EXTRA_MODE_EDIT

            val dueMillisToReturn = selectedDueTimeMillis
                ?: originalDueTimeMillis
                ?: -1L
            val resultIntent = Intent().apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_DESCRIPTION, description)
                putExtra(EXTRA_DUE_DATE, dueDate)
                putExtra(EXTRA_PRIORITY, priority)
                putExtra(EXTRA_DUE_TIME_MILLIS, dueMillisToReturn)
                putExtra(EXTRA_IS_EDIT, isEdit)
                if (isEdit) {
                    putExtra(EXTRA_TASK_ID, originalTaskId)
                }
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}

