package com.example.taskmasterfinalproject.addtask

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmasterfinalproject.R
import com.example.taskmasterfinalproject.databinding.ActivityAddTaskBinding
import com.example.taskmasterfinalproject.util.setupBackNavigation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "extra_task_title"
        const val EXTRA_DESCRIPTION = "extra_task_description"
        const val EXTRA_DUE_DATE = "extra_task_due_date"
        const val EXTRA_PRIORITY = "extra_task_priority"
        const val EXTRA_DUE_TIME_MILLIS = "extra_due_time_millis"
    }

    private lateinit var binding: ActivityAddTaskBinding
    private var dueTimeInMillis: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        com.example.taskmasterfinalproject.settings.ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editTaskDueDate.setOnClickListener {
            showDateTimePicker()
        }
        
        // Setup Toolbar
        setupBackNavigation(binding.toolbar, getString(R.string.add_task_title))

        setupPrioritySpinner()
        setupButtons()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupPrioritySpinner() {
        val priorities = resources.getStringArray(R.array.task_priority_entries)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            priorities
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTaskPriority.adapter = adapter
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
                binding.editTaskDueDate.setText(formatted)
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
        binding.buttonCancelTask.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        binding.buttonSaveTask.setOnClickListener {
            val title = binding.editTaskTitle.text.toString()
            if (title.isBlank()) {
                binding.editTaskTitle.error = getString(R.string.error_empty_title)
                return@setOnClickListener
            }
            val description = binding.editTaskDescription.text.toString()
            val dueDate = binding.editTaskDueDate.text.toString()

            val selectedPosition = binding.spinnerTaskPriority.selectedItemPosition
            val priority = if (selectedPosition in 0..2) selectedPosition + 1 else 0

            val resultIntent = Intent().apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_DESCRIPTION, description)
                putExtra(EXTRA_DUE_DATE, dueDate)
                putExtra(EXTRA_PRIORITY, priority)
                putExtra(EXTRA_DUE_TIME_MILLIS, dueTimeInMillis ?: -1L)
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
