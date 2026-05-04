package com.example.taskmasterfinalproject.util

// מחלקת הבסיס ל-Activity
import androidx.appcompat.app.AppCompatActivity
// ספרייה לרכיב סרגל הכלים (Toolbar)
import androidx.appcompat.widget.Toolbar

/**
 * פונקציית הרחבה להגדרת סרגל כלים עם חץ חזרה.
 * יש לקרוא לפונקציה זו ב-onCreate() של ה-Activity.
 *
 * חשוב: חובה לדרוס גם את onSupportNavigateUp() ב-Activity:
 *
 * override fun onSupportNavigateUp(): Boolean {
 *     onBackPressedDispatcher.onBackPressed()
 *     return true
 * }
 */
fun AppCompatActivity.setupBackNavigation(toolbar: Toolbar, title: String? = null) {
    setSupportActionBar(toolbar)
    supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(true)
        setDisplayShowHomeEnabled(true)
        if (title != null) {
            this.title = title
        }
    }
}
