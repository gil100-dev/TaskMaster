package com.example.taskmasterfinalproject.util

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 * Sets up the Toolbar with a Back (Up) arrow.
 * Call this in your Activity's onCreate().
 * 
 * IMPORTANT: You must also override onSupportNavigateUp() in your Activity:
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
