package com.example.taskmasterfinalproject.main

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmasterfinalproject.R
import com.example.taskmasterfinalproject.addtask.AddTaskActivity
import com.example.taskmasterfinalproject.databinding.FragmentTaskListBinding
import com.example.taskmasterfinalproject.details.TaskDetailActivity
import com.example.taskmasterfinalproject.model.SortOption
import com.google.android.material.snackbar.Snackbar

class TaskListBindingWrapper(val binding: FragmentTaskListBinding)

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels() // Share ViewModel with Host
    private lateinit var taskAdapter: TaskAdapter

    private val addTaskLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                // We rely on MainActivity to have handled logic OR we handle it here.
                // Actually, the Intent result usually goes to whoever launched it.
                // So we copy the logic from MainActivity here.
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

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

    private fun setupRecyclerView() {
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
            }
        )
        binding.recyclerTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTasks.adapter = taskAdapter
        setupSwipeToDelete(binding.recyclerTasks)
    }

    private fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            private val deleteColor = Color.RED
            private val completeColor = Color.parseColor("#4CAF50") // Green
            private val paint = Paint()

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

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
                    val iconMargin = (itemView.height - 50) / 2 // Approximate margin
                    val textPaint = Paint().apply {
                        color = Color.WHITE
                        textSize = 40f
                        isAntiAlias = true
                        textAlign = Paint.Align.LEFT
                    }

                    if (dX > 0) { // Right Swipe (Complete)
                        // Background
                        paint.color = completeColor
                        c.drawRect(
                            itemView.left.toFloat(), itemView.top.toFloat(),
                            dX, itemView.bottom.toFloat(), paint
                        )
                        
                        // Icon
                        val icon = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
                        icon?.let {
                            val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                            val iconBottom = iconTop + it.intrinsicHeight
                            val iconLeft = itemView.left + iconMargin
                            val iconRight = iconLeft + it.intrinsicWidth
                            it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                            // Only draw if swipe is large enough
                            if (dX > iconRight + 20) {
                                it.draw(c)
                                // Text
                                c.drawText("Completed", (iconRight + 20).toFloat(), (itemView.top + itemView.bottom) / 2f + 15, textPaint)
                            }
                        }

                    } else if (dX < 0) { // Left Swipe (Delete)
                        // Background
                        paint.color = deleteColor
                        c.drawRect(
                            itemView.right.toFloat() + dX, itemView.top.toFloat(),
                            itemView.right.toFloat(), itemView.bottom.toFloat(), paint
                        )

                        // Icon
                        val icon = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
                        icon?.let {
                            val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                            val iconBottom = iconTop + it.intrinsicHeight
                            val iconRight = itemView.right - iconMargin
                            val iconLeft = iconRight - it.intrinsicWidth
                            it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                            
                            // Only draw if swipe is large enough
                            if (-dX > (itemView.right - iconLeft) + 20) {
                                it.draw(c)
                                // Text
                                textPaint.textAlign = Paint.Align.RIGHT
                                c.drawText("Delete", (iconLeft - 20).toFloat(), (itemView.top + itemView.bottom) / 2f + 15, textPaint)
                            }
                        }
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

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
            else -> 0 // Safety: Default to 0 if unknown or DEFAULT
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
