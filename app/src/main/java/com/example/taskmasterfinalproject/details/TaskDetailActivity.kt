package com.example.taskmasterfinalproject.details

// ספרייה להעברת מידע בין רכיבים (Bundle)
import android.os.Bundle
// ספרייה ליצירת ViewModels בתוך Activity
import androidx.activity.viewModels
// מחלקת בסיס ל-Activities התואמת גרסאות ישנות
import androidx.appcompat.app.AppCompatActivity
// ספרייה לרכיבי ארכיטקטורה (ViewModel)
import androidx.lifecycle.ViewModel
// מפעל ליצירת ViewModels
import androidx.lifecycle.ViewModelProvider
// מנהל תצוגה לרשימות (ליניארי)
import androidx.recyclerview.widget.LinearLayoutManager
// שכבת הנתונים (Repository) לניהול משימות
import com.example.taskmasterfinalproject.data.TaskRepository
// מסד הנתונים של האפליקציה
import com.example.taskmasterfinalproject.db.TaskDatabase
// מחלקת ה-Binding עבור מסך פרטי המשימה
import com.example.taskmasterfinalproject.databinding.ActivityTaskDetailBinding
// מנהל טקסט-לדיבור (TTS)
import com.example.taskmasterfinalproject.audio.TextToSpeechManager
// פונקציית עזר לניווט חזור
import com.example.taskmasterfinalproject.util.setupBackNavigation

// מסך המציג את פרטי המשימה ומאפשר עריכתם, יורש מ-AppCompatActivity
class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailBinding
    private lateinit var subtaskAdapter: SubtaskAdapter
    // מופע של מנהל הטקסט-לדיבור
    private lateinit var ttsManager: TextToSpeechManager

    // יצירת ה-ViewModel עם Factory מותאם אישית להזרקת תלויות
    private val viewModel: TaskDetailViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val taskDao = TaskDatabase.getInstance(applicationContext).taskDao()
                val repository = TaskRepository(taskDao)
                val taskId = intent.getStringExtra("TASK_ID") ?: ""
                @Suppress("UNCHECKED_CAST")
                return TaskDetailViewModel(repository, taskId) as T
            }
        }
    }

    // פונקציית ה-Lifecycle הראשית, נקראת ביצירת המסך
    override fun onCreate(savedInstanceState: Bundle?) {
        com.example.taskmasterfinalproject.settings.ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // הגדרת סרגל הכלים (Toolbar)
        setupBackNavigation(binding.toolbar, "Task Details")

        val taskId = intent.getStringExtra(EXTRA_TASK_ID)
        if (taskId == null) {
            android.widget.Toast.makeText(this, "Error: Task not found", android.widget.Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // אתחול מנהל TTS
        ttsManager = TextToSpeechManager(this)

        setupRecyclerView()
        observeViewModel()

        // לחיצה על כפתור ההקראה – מרכיב את הטקסט מפרטי המשימה ומקריא
        binding.buttonSpeakTask.setOnClickListener {
            currentTask?.let { task ->
                val spokenText = buildSpokenText(task)
                ttsManager.speak(spokenText)
            }
        }

        binding.buttonAddSubtask.setOnClickListener {
            val title = binding.editNewSubtask.text.toString()
            if (title.isNotBlank()) {
                viewModel.addSubtask(title)
                binding.editNewSubtask.text.clear()
                hideKeyboard()
            }
        }
        
        binding.fabAiDetail.setOnClickListener {
            val subtasks = viewModel.taskWithDetails.value?.subtasks?.map { it.title } ?: emptyList()
            val fragment = com.example.taskmasterfinalproject.ai.AiCoachBottomSheet.newInstance(currentTask, subtasks)
            fragment.onApplySubtasks = { generatedSubtasks ->
                generatedSubtasks.forEach { sub ->
                     // הוספת משך זמן לכותרת לצורך תצוגה
                    viewModel.addSubtask("${sub.title} (~${sub.minutes}m)")
                }
            }
            fragment.show(supportFragmentManager, com.example.taskmasterfinalproject.ai.AiCoachBottomSheet.TAG)
        }
    }
    
    private var currentTask: com.example.taskmasterfinalproject.model.Task? = null

    // פונקציה להגדרת ה-RecyclerView וה-Adapter
    private fun setupRecyclerView() {
        subtaskAdapter = SubtaskAdapter(
            onSubtaskToggled = { subtask ->
                viewModel.toggleSubtask(subtask)
            },
            onSubtaskDeleted = { subtask ->
                viewModel.deleteSubtask(subtask)
                com.google.android.material.snackbar.Snackbar.make(binding.root, "Subtask deleted", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        viewModel.restoreSubtask(subtask)
                    }
                    .show()
            }
        )
        binding.recyclerSubtasks.apply {
            layoutManager = LinearLayoutManager(this@TaskDetailActivity)
            adapter = subtaskAdapter
        }
    }
    
    // פונקציה להסתרת המקלדת
    private fun hideKeyboard() {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
        currentFocus?.let {
            imm?.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    // פונקציה להאזנה לשינויים בנתונים מתוך ה-ViewModel
    private fun observeViewModel() {
        viewModel.taskWithDetails.observe(this) { taskWithDetails ->
            taskWithDetails?.let {
                currentTask = it.task
                binding.textTaskTitle.text = it.task.title
                subtaskAdapter.submitList(it.subtasks)

                val doneCount = it.subtasks.count { s -> s.isDone }
                val totalCount = it.subtasks.size
                binding.textProgress.text = "$doneCount / $totalCount"
                binding.progressSubtasks.max = totalCount
                binding.progressSubtasks.progress = doneCount

                binding.chipGroupTags.removeAllViews()
                it.tags.forEach { tag ->
                    val chip = com.google.android.material.chip.Chip(this)
                    chip.text = tag.name
                    binding.chipGroupTags.addView(chip)
                }
            }
        }
    }

    // פונקציה לבניית טקסט מדובר מפרטי המשימה
    private fun buildSpokenText(task: com.example.taskmasterfinalproject.model.Task): String {
        val sb = StringBuilder()
        sb.append(getString(com.example.taskmasterfinalproject.R.string.tts_task_prefix))
        sb.append(task.title ?: "")
        if (!task.description.isNullOrBlank()) {
            sb.append(". ")
            sb.append(getString(com.example.taskmasterfinalproject.R.string.tts_description_prefix))
            sb.append(task.description)
        }
        if (!task.dueDate.isNullOrBlank()) {
            sb.append(". ")
            sb.append(getString(com.example.taskmasterfinalproject.R.string.tts_due_date_prefix))
            sb.append(task.dueDate)
        }
        return sb.toString()
    }

    // פונקציה לטיפול בלחיצה על כפתור החזור בסרגל הכלים
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // שחרור משאבי TTS כאשר ה-Activity נהרס
    override fun onDestroy() {
        ttsManager.shutdown()
        super.onDestroy()
    }

    // אובייקט נלווה המכיל קבועים סטטיים
    companion object {
        const val EXTRA_TASK_ID = "TASK_ID"
    }
}
