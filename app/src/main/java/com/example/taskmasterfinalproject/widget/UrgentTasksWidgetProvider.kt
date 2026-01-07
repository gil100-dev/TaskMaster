package com.example.taskmasterfinalproject.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.example.taskmasterfinalproject.R
import com.example.taskmasterfinalproject.db.TaskDatabase
import com.example.taskmasterfinalproject.details.TaskDetailActivity
import com.example.taskmasterfinalproject.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class UrgentTasksWidgetProvider : AppWidgetProvider() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Perform this loop procedure for each AppWidget that belongs to this provider
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        job.cancel()
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        
        // Launch coroutine to fetch data
        scope.launch {
            val views = RemoteViews(context.packageName, R.layout.widget_urgent_tasks)
            
            // Setup Refresh Button
            val refreshIntent = Intent(context, UrgentTasksWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context, 
                appWidgetId, 
                refreshIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.button_refresh, refreshPendingIntent)

            // Fetch Data
            val urgentTasks = withContext(Dispatchers.IO) {
                try {
                    val dao = TaskDatabase.getInstance(context).taskDao()
                    val allTasks = dao.getTasksList() // Using new sync method
                    
                    // Filter and Sort Logic:
                    // 1. Not Completed
                    // 2. Priority High (1) -> Low (3)
                    // 3. Deadline Soon -> Late
                    allTasks.filter { !it.isCompleted }
                        .sortedWith(compareBy<Task> { it.priority ?: Int.MAX_VALUE }
                            .thenBy { if (it.dueTimeMillis != null && it.dueTimeMillis > 0) it.dueTimeMillis else Long.MAX_VALUE })
                        .take(3)
                } catch (e: Exception) {
                    emptyList()
                }
            }

            // Update UI
            if (urgentTasks.isEmpty()) {
                views.setViewVisibility(R.id.text_empty, View.VISIBLE)
                views.setViewVisibility(R.id.container_task_1, View.GONE)
                views.setViewVisibility(R.id.container_task_2, View.GONE)
                views.setViewVisibility(R.id.container_task_3, View.GONE)
            } else {
                views.setViewVisibility(R.id.text_empty, View.GONE)
                
                // Helper to update row
                fun updateRow(index: Int, containerId: Int, titleId: Int, detailId: Int) {
                    if (index < urgentTasks.size) {
                        val task = urgentTasks[index]
                        views.setViewVisibility(containerId, View.VISIBLE)
                        views.setTextViewText(titleId, task.title)
                        
                        val deadlineStr = if (task.dueTimeMillis != null && task.dueTimeMillis > 0) {
                             val sdf = SimpleDateFormat("MMM dd HH:mm", Locale.getDefault())
                             sdf.format(java.util.Date(task.dueTimeMillis))
                        } else {
                            "No deadline"
                        }
                        views.setTextViewText(detailId, deadlineStr)
                        
                        // Click Intent
                        val detailIntent = Intent(context, TaskDetailActivity::class.java).apply {
                            putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.id)
                            data = Uri.parse("content://task/${task.id}") // Unique Data for Intent
                        }
                        val detailPendingIntent = PendingIntent.getActivity(
                            context,
                            task.id.hashCode(), // Unique Request Code
                            detailIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(containerId, detailPendingIntent)
                        
                    } else {
                        views.setViewVisibility(containerId, View.GONE)
                    }
                }

                updateRow(0, R.id.container_task_1, R.id.text_title_1, R.id.text_detail_1)
                updateRow(1, R.id.container_task_2, R.id.text_title_2, R.id.text_detail_2)
                updateRow(2, R.id.container_task_3, R.id.text_title_3, R.id.text_detail_3)
            }

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
