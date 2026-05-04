package com.example.taskmasterfinalproject.main

// ספרייה ליצירת דיאלוגים (חלונות קופצים)
import android.app.AlertDialog
// ספרייה ליצירת Intents למעבר בין מסכים
import android.content.Intent
// ספרייה לציור גרפי על גבי קנבס
import android.graphics.Canvas
// ספרייה לניהול צבעים
import android.graphics.Color
// ספרייה להגדרת סגנונות ציור (מברשת)
import android.graphics.Paint
// ספרייה להעברת נתונים (Bundle)
import android.os.Bundle
// ספרייה לניהול יצירת Views
import android.view.LayoutInflater
// ספרייה המייצגת אלמנט תצוגה
import android.view.View
// ספרייה המייצגת קבוצת Views
import android.view.ViewGroup
// ספרייה לניהול אירועי בחירה ברשימות/ספינרים
import android.widget.AdapterView
// ספרייה לקישור מערך נתונים לתצוגה (Adapter פשוט)
import android.widget.ArrayAdapter
// מחלקת בסיס ל-Fragment (חלק ממסך)
import androidx.fragment.app.Fragment
// ספרייה לשיתוף ViewModel עם ה-Activity המארח
import androidx.fragment.app.activityViewModels
// ספרייה לניהול אינטראקציות גרירה והחלקה ב-RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
// מנהל תצוגה ליניארי עבור RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
// ספרייה לרכיב ה-RecyclerView (רשימה נגללת)
import androidx.recyclerview.widget.RecyclerView
// משאבי האפליקציה
import com.example.taskmasterfinalproject.R
// Activity להוספת משימה חדשה
import com.example.taskmasterfinalproject.addtask.AddTaskActivity
// מחלקת ה-Binding של הפרגמנט
import com.example.taskmasterfinalproject.databinding.FragmentTaskListBinding
// Activity להצגת פרטי משימה
import com.example.taskmasterfinalproject.details.TaskDetailActivity
// המודל המייצג אפשרויות מיון
import com.example.taskmasterfinalproject.model.SortOption
// מנהל טקסט-לדיבור (TTS)
import com.example.taskmasterfinalproject.audio.TextToSpeechManager
// רכיב להצגת הודעות קצרות בתחתית המסך
import com.google.android.material.snackbar.Snackbar

// מחלקה עוטפת ל-Binding, לשימוש פנימי
class TaskListBindingWrapper(val binding: FragmentTaskListBinding)

// פרגמנט המציג את רשימת המשימות הראשית
class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels() // משתף ViewModel עם ה-Host
    private lateinit var taskAdapter: TaskAdapter
    // מופע של מנהל הטקסט-לדיבור
    private var ttsManager: TextToSpeechManager? = null

    // משתנה לטיפול בתוצאה החוזרת ממסך הוספת משימה
    private val addTaskLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                // אנו מסתמכים על MainActivity או מטפלים בזה כאן.
                // למעשה, תוצאת ה-Intent מגיעה למי שיזם אותה.
                // אז נשכפל את הלוגיקה מכאן.
                val title = data.getStringExtra(AddTaskActivity.EXTRA_TITLE)
                val description = data.getStringExtra(AddTaskActivity.EXTRA_DESCRIPTION)
                val dueDate = data.getStringExtra(AddTaskActivity.EXTRA_DUE_DATE)
                val priority = data.getIntExtra(AddTaskActivity.EXTRA_PRIORITY, 0)
                val dueTimeMillis = data.getLongExtra(AddTaskActivity.EXTRA_DUE_TIME_MILLIS, -1L)
                val millisOrNull = if (dueTimeMillis > 0) dueTimeMillis else null

                val newTask = com.example.taskmasterfinalproject.model.Task(
                    id = System.currentTimeMillis().toString(),
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    dueTimeMillis = millisOrNull
                )
                viewModel.addTask(newTask)
                
                Snackbar.make(binding.root, "Task added successfully", Snackbar.LENGTH_SHORT).show()

                if (millisOrNull != null) {
                   com.example.taskmasterfinalproject.notifications.ReminderScheduler.scheduleTaskReminder(requireContext(), newTask)
                }
            }
        }
    }

    // פונקציה ליצירת התצוגה של הפרגמנט
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    // פונקציה הנקראת לאחר שהתצוגה נוצרה, משמשת לאיתחול לוגיקה
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSortSpinner()
        
        binding.buttonFilter.setOnClickListener {
            viewModel.toggleShowCompleted()
            Snackbar.make(binding.root, "Toggled completed tasks", Snackbar.LENGTH_SHORT).show()
        }

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
            binding.emptyStateContainer.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAddTask.setOnClickListener {
            val intent = Intent(requireContext(), AddTaskActivity::class.java)
            addTaskLauncher.launch(intent)
        }
    }

    // פונקציה להגדרת ה-RecyclerView וה-Adapter
    private fun setupRecyclerView() {
        // אתחול מנהל TTS
        ttsManager = TextToSpeechManager(requireContext())

        taskAdapter = TaskAdapter(
            emptyList(),
            onTaskClick = { task ->
                val intent = Intent(requireContext(), TaskDetailActivity::class.java)
                intent.putExtra("TASK_ID", task.id)
                startActivity(intent)
            },
            onTaskComplete = { task ->
                viewModel.markTaskCompleted(task)
            },
            onTaskDelete = { task ->
                viewModel.deleteTask(task)
            },
            onTaskSpeak = { task ->
                // בניית הטקסט המדובר והקראה
                val spokenText = buildSpokenText(task)
                ttsManager?.speak(spokenText)
            }
        )
        binding.recyclerTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTasks.adapter = taskAdapter
        setupSwipeToDelete(binding.recyclerTasks)
    }

    // פונקציה להגדרת המחוות של החלקה למחיקה/השלמה
    private fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            private val deleteColor = Color.RED
            private val completeColor = Color.parseColor("#4CAF50") // ירוק
            private val paint = Paint()

            // פונקציה לטיפול בגרירה (לא בשימוש כאן)
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            // פונקציה לציור הרקע והאייקון בזמן החלקה
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val iconMargin = (itemView.height - 50) / 2 // שוליים משוערים
                    val textPaint = Paint().apply {
                        color = Color.WHITE
                        textSize = 40f
                        isAntiAlias = true
                        textAlign = Paint.Align.LEFT
                    }

                    if (dX > 0) { // החלקה ימינה (השלמה)
                        // רקע
                        paint.color = completeColor
                        c.drawRect(
                            itemView.left.toFloat(), itemView.top.toFloat(),
                            dX, itemView.bottom.toFloat(), paint
                        )
                        
                        // אייקון
                        val icon = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
                        icon?.let {
                            val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                            val iconBottom = iconTop + it.intrinsicHeight
                            val iconLeft = itemView.left + iconMargin
                            val iconRight = iconLeft + it.intrinsicWidth
                            it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                            // ציור רק אם ההחלקה גדולה מספיק
                            if (dX > iconRight + 20) {
                                it.draw(c)
                                // טקסט
                                c.drawText("Completed", (iconRight + 20).toFloat(), (itemView.top + itemView.bottom) / 2f + 15, textPaint)
                            }
                        }

                    } else if (dX < 0) { // החלקה שמאלה (מחיקה)
                        // רקע
                        paint.color = deleteColor
                        c.drawRect(
                            itemView.right.toFloat() + dX, itemView.top.toFloat(),
                            itemView.right.toFloat(), itemView.bottom.toFloat(), paint
                        )

                        // אייקון
                        val icon = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
                        icon?.let {
                            val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                            val iconBottom = iconTop + it.intrinsicHeight
                            val iconRight = itemView.right - iconMargin
                            val iconLeft = iconRight - it.intrinsicWidth
                            it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                            
                            // ציור רק אם ההחלקה גדולה מספיק
                            if (-dX > (itemView.right - iconLeft) + 20) {
                                it.draw(c)
                                // טקסט
                                textPaint.textAlign = Paint.Align.RIGHT
                                c.drawText("Delete", (iconLeft - 20).toFloat(), (itemView.top + itemView.bottom) / 2f + 15, textPaint)
                            }
                        }
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            // פונקציה הנקראת בסיום ההחלקה
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = taskAdapter.getTask(position)

                if (direction == ItemTouchHelper.LEFT) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to delete '${task.title}'?")
                        .setPositiveButton("Delete") { _, _ ->
                            viewModel.deleteTask(task)
                            Snackbar.make(recyclerView, "Task deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO") { viewModel.addTask(task) }
                                .show()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                            taskAdapter.notifyItemChanged(position)
                        }
                        .setOnCancelListener { taskAdapter.notifyItemChanged(position) }
                        .show()

                } else if (direction == ItemTouchHelper.RIGHT) {
                    viewModel.markTaskCompleted(task)
                    com.example.taskmasterfinalproject.audio.SoundManager.playPrioritySound(task.priority ?: 0)
                    Snackbar.make(recyclerView, "Task completed", Snackbar.LENGTH_LONG)
                        .setAction("UNDO") { 
                           val reverted = task.copy(isCompleted = false, completedAt = null)
                           viewModel.updateTask(reverted)
                        }
                        .show()
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }

    // פונקציה להגדרת הספינר לבחירת מיון
    private fun setupSortSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSort.adapter = adapter
        }

        val currentSort = viewModel.currentSortOption
        val position = when (currentSort) {
            SortOption.BY_DATE -> 0
            SortOption.BY_PRIORITY -> 1
            SortOption.SMART_SORT -> 2
            else -> 0 // בטיחות: ברירת מחדל ל-0 אם לא ידוע
        }
        if (position in 0 until binding.spinnerSort.adapter.count) {
            binding.spinnerSort.setSelection(position, false)
        }

        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val sortOption = when (position) {
                    0 -> SortOption.BY_DATE
                    1 -> SortOption.BY_PRIORITY
                    2 -> SortOption.SMART_SORT
                    else -> SortOption.DEFAULT
                }
                viewModel.setSortOption(sortOption)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // פונקציה לבניית טקסט מדובר מפרטי המשימה
    private fun buildSpokenText(task: com.example.taskmasterfinalproject.model.Task): String {
        val sb = StringBuilder()
        sb.append(getString(R.string.tts_task_prefix))
        sb.append(task.title ?: "")
        if (!task.description.isNullOrBlank()) {
            sb.append(". ")
            sb.append(getString(R.string.tts_description_prefix))
            sb.append(task.description)
        }
        if (!task.dueDate.isNullOrBlank()) {
            sb.append(". ")
            sb.append(getString(R.string.tts_due_date_prefix))
            sb.append(task.dueDate)
        }
        return sb.toString()
    }

    // נקראת כשהתצוגה נהרסת, לניקוי משאבים
    override fun onDestroyView() {
        ttsManager?.shutdown()
        ttsManager = null
        super.onDestroyView()
        _binding = null
    }
}
