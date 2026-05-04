package com.example.taskmasterfinalproject.addtask

// ספרייה המייצגת Activity (לצורך תוצאות activity)
import android.app.Activity
// ספרייה ליצירת דיאלוג התראה
import android.app.AlertDialog
// ספרייה ליצירת דיאלוג בחירת תאריך
import android.app.DatePickerDialog
// ספרייה ליצירת דיאלוג בחירת שעה
import android.app.TimePickerDialog
// ספרייה ליצירת Intents למעבר והחזרת נתונים
import android.content.Intent
// ספרייה לבדיקת הרשאות
import android.content.pm.PackageManager
// ספרייה להעברת נתונים בין רכיבים (Bundle)
import android.os.Bundle
// ספרייה להצגת הודעות קצרות (Toast)
import android.widget.Toast
// ספרייה לקישור מערך נתונים לרכיבי UI כמו Spinner
import android.widget.ArrayAdapter
// ספרייה לתוצאות Activity (ActivityResultLauncher)
import androidx.activity.result.contract.ActivityResultContracts
// מחלקת בסיס ל-Activities התואמת גרסאות ישנות
import androidx.appcompat.app.AppCompatActivity
// ספרייה לבדיקת הרשאות אפליקציה
import androidx.core.app.ActivityCompat
// ספרייה לבדיקת הרשאות ליבה
import androidx.core.content.ContextCompat
// משאבי האפליקציה
import com.example.taskmasterfinalproject.R
// מנהל קלט קולי (Speech-To-Text)
import com.example.taskmasterfinalproject.audio.SpeechToTextManager
// מחלקת ה-Binding של מסך הוספת משימה
import com.example.taskmasterfinalproject.databinding.ActivityAddTaskBinding
// פונקציית עזר לניווט חזור
import com.example.taskmasterfinalproject.util.setupBackNavigation
// ספרייה לפרמוט תאריכים
import java.text.SimpleDateFormat
// ספרייה לטיפול בתאריכים וזמנים
import java.util.Calendar
// ספרייה להגדרת אזור (Locale) לצורך פורמט תאריך
import java.util.Locale

// מסך להוספת משימה חדשה
class AddTaskActivity : AppCompatActivity() {

    // אובייקט המכיל קבועים סטטיים (שמות מפתחות להעברת נתונים ב-Intent)
    companion object {
        const val EXTRA_TITLE = "extra_task_title"
        const val EXTRA_DESCRIPTION = "extra_task_description"
        const val EXTRA_DUE_DATE = "extra_task_due_date"
        const val EXTRA_PRIORITY = "extra_task_priority"
        const val EXTRA_DUE_TIME_MILLIS = "extra_due_time_millis"
    }

    private lateinit var binding: ActivityAddTaskBinding
    private var dueTimeInMillis: Long? = null

    // משתנה לטיפול בתוצאת זיהוי הדיבור
    private val voiceInputLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val recognizedText = SpeechToTextManager.extractRecognizedText(result.data)
            if (recognizedText != null) {
                handleRecognizedText(recognizedText)
            } else {
                Toast.makeText(this, getString(R.string.stt_no_speech), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // משתנה לטיפול בתוצאת בקשת הרשאת מיקרופון
    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchVoiceInput()
        } else {
            Toast.makeText(this, getString(R.string.stt_error), Toast.LENGTH_SHORT).show()
        }
    }

    // פונקציית ה-Lifecycle הראשית
    override fun onCreate(savedInstanceState: Bundle?) {
        com.example.taskmasterfinalproject.settings.ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editTaskDueDate.setOnClickListener {
            showDateTimePicker()
        }
        
        // הגדרת סרגל הכלים
        setupBackNavigation(binding.toolbar, getString(R.string.add_task_title))

        setupPrioritySpinner()
        setupButtons()
        setupVoiceInput()
    }
    
    // טיפול בלחיצה על כפתור חזור בסרגל הכלים
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // הגדרת הספינר לבחירת עדיפות המשימה
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

    // הצגת דיאלוג לבחירת תאריך ושעה
    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                // שמירת הזמן שנבחר
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

    // הגדרת כפתור הקלט הקולי
    private fun setupVoiceInput() {
        binding.buttonVoiceInput.setOnClickListener {
            // בדיקת הרשאת מיקרופון
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
            ) {
                launchVoiceInput()
            } else {
                micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // הפעלת זיהוי דיבור
    private fun launchVoiceInput() {
        try {
            val intent = SpeechToTextManager.createVoiceInputIntent()
            voiceInputLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.stt_not_available), Toast.LENGTH_SHORT).show()
        }
    }

    // טיפול בטקסט שזוהה מהדיבור
    private fun handleRecognizedText(text: String) {
        val currentTitle = binding.editTaskTitle.text.toString()

        if (currentTitle.isNotBlank()) {
            // אם כבר יש טקסט – שואלים את המשתמש אם להחליף
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.stt_overwrite_confirm))
                .setMessage(currentTitle)
                .setPositiveButton("כן") { _, _ ->
                    applyRecognizedText(text)
                }
                .setNegativeButton("לא", null)
                .show()
        } else {
            applyRecognizedText(text)
        }
    }

    // החלת הטקסט שזוהה על שדות הכותרת והתיאור
    private fun applyRecognizedText(text: String) {
        val (title, description) = SpeechToTextManager.splitToTitleAndDescription(text)
        binding.editTaskTitle.setText(title)
        if (description != null && binding.editTaskDescription.text.toString().isBlank()) {
            binding.editTaskDescription.setText(description)
        }
    }

    // הגדרת כפתורי שמירה וביטול
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
