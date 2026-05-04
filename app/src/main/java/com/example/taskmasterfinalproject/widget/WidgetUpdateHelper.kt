package com.example.taskmasterfinalproject.widget

// מנהל הווידג'טים
import android.appwidget.AppWidgetManager
// ספרייה לזיהוי רכיב אפליקציה
import android.content.ComponentName
// ספרייה למשאבי המערכת
import android.content.Context
// ספרייה ליצירת Intents
import android.content.Intent

// אובייקט עזר לעדכון ידני של הווידג'ט מתוך האפליקציה (למשל לאחר הוספת משימה)
object WidgetUpdateHelper {
    // פונקציה לשליחת בקשת עדכון לווידג'ט המשימות הדחופות
    fun updateUrgentTasksWidget(context: Context) {
        val intent = Intent(context, UrgentTasksWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, UrgentTasksWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }
}
