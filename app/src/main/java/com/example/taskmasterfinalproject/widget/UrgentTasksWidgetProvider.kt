package com.example.taskmasterfinalproject.widget

// ספרייה לעטיפת Intent לביצוע עתידי
import android.app.PendingIntent
// מנהל הווידג'טים של המערכת
import android.appwidget.AppWidgetManager
// מחלקת בסיס לספק (Provider) של ווידג'ט
import android.appwidget.AppWidgetProvider
// ספרייה המספקת גישה למשאבי המערכת
import android.content.Context
// ספרייה ליצירת Intents
import android.content.Intent
// ספרייה לטיפול בכתובות URI
import android.net.Uri
// ספרייה לניהול רכיבי תצוגה
import android.view.View
// ספרייה לבניית ממשק משתמש עבור ווידג'טים (RemoteViews)
import android.widget.RemoteViews
// משאבי האפליקציה
import com.example.taskmasterfinalproject.R
// מסד הנתונים
import com.example.taskmasterfinalproject.db.TaskDatabase
// המסך לפרטי משימה
import com.example.taskmasterfinalproject.details.TaskDetailActivity
// מודל המשימה
import com.example.taskmasterfinalproject.model.Task
// ספרייה לניהול Coroutines
import kotlinx.coroutines.CoroutineScope
// ה-Dispatcher הראשי של ה-UI
import kotlinx.coroutines.Dispatchers
// Job המאפשר כישלון של ילדים מבלי להפיל את ההורה
import kotlinx.coroutines.SupervisorJob
// ספרייה להרצת קורוטינה
import kotlinx.coroutines.launch
// ספרייה להחלפת Dispatcher
import kotlinx.coroutines.withContext
// ספרייה לפרמוט תאריכים
import java.text.SimpleDateFormat
// הגדרות אזור
import java.util.Locale

// ספק הווידג'ט המציג משימות דחופות במסך הבית
class UrgentTasksWidgetProvider : AppWidgetProvider() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    // נקרא כאשר יש לעדכן את הווידג'ט
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // בצע לולאה עבור כל מופע של הווידג'ט ששיך לספק זה
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    // נקרא כאשר הווידג'ט נמחק
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // כאשר המשתמש מוחק את הווידג'ט, מחיקת העדפות הקשורות אליו אם יש.
    }

    // נקרא כאשר המופע האחרון של הווידג'ט הוסר
    override fun onDisabled(context: Context) {
        // ביטול ה-Job כדי למנוע דליפות זיכרון
        job.cancel()
    }

    // פונקציה לעדכון תצוגת הווידג'ט
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        
        // הרצת קורוטינה לשליפת נתונים
        scope.launch {
            val views = RemoteViews(context.packageName, R.layout.widget_urgent_tasks)
            
            // הגדרת כפתור רענון
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

                val urgentTasks = withContext(Dispatchers.IO) {
                try {
                    val prefs = com.example.taskmasterfinalproject.data.PreferencesManager(context)
                    val userId = prefs.getSessionUserId()
                    
                    if (userId == null) {
                        emptyList()
                    } else {
                        val dao = TaskDatabase.getInstance(context).taskDao()
                        val allTasks = dao.getTasksList(userId) // שימוש בשיטה מסונכרנת חדשה
                        
                        // לוגיקת סינון ומיון:
                        // 1. לא הושלמו
                        // 2. עדיפות גבוהה (3) -> נמוכה (1)
                        // 3. זמן יעד קרוב -> רחוק
                        allTasks.filter { !it.isCompleted }
                            .sortedWith(compareByDescending<Task> { it.priority ?: 0 }
                                .thenBy { if (it.dueTimeMillis != null && it.dueTimeMillis > 0) it.dueTimeMillis else Long.MAX_VALUE })
                            .take(3)
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }

            // עדכון ה-UI
            if (urgentTasks.isEmpty()) {
                views.setViewVisibility(R.id.text_empty, View.VISIBLE)
                views.setViewVisibility(R.id.container_task_1, View.GONE)
                views.setViewVisibility(R.id.container_task_2, View.GONE)
                views.setViewVisibility(R.id.container_task_3, View.GONE)
            } else {
                views.setViewVisibility(R.id.text_empty, View.GONE)
                
                // פונקציית עזר לעדכון שורה
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
                        
                        // הגדרת לחיצה למעבר לפרטי המשימה
                        val detailIntent = Intent(context, TaskDetailActivity::class.java).apply {
                            putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.id)
                            data = Uri.parse("content://task/${task.id}") // Data ייחודי ל-Intent
                        }
                        val detailPendingIntent = PendingIntent.getActivity(
                            context,
                            task.id.hashCode(), // קוד ייחודי
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

            // הוראה למנהל הווידג'טים לעדכן את התצוגה
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
