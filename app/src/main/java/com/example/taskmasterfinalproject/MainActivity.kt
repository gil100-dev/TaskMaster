package com.example.taskmasterfinalproject

// ספרייה בסיסית של אנדרואיד המייצגת מסך
import android.app.Activity
// ספרייה ליצירת Intents למעבר בין מסכים
import android.content.Intent
// ספרייה לניהול הרשאות
import android.content.pm.PackageManager
// ספרייה המכילה מידע על גרסת המערכת
import android.os.Build
// ספרייה להעברת נתונים בין רכיבים
import android.os.Bundle
// ספרייה לטיפול בתוצאות מ-StartActivityForResult בסגנון חדש
import androidx.activity.result.contract.ActivityResultContracts
// ספרייה ליצירת ViewModels בתוך Activity
import androidx.activity.viewModels
// מחלקת בסיס ל-Activities התואמת גרסאות ישנות
import androidx.appcompat.app.AppCompatActivity
// ספרייה לטיפול בתאימות של Views
import androidx.core.view.ViewCompat
// ספרייה לטיפול ב-WindowInsets (כגון סרגל סטטוס)
import androidx.core.view.WindowInsetsCompat
// ספרייה לניהול אינטראקציות גרירה והחלקה (לא בשימוש ישיר כאן אך מיובאת)
import androidx.recyclerview.widget.ItemTouchHelper
// ספרייה לניהול פריסה (Layout) ברשימות
import androidx.recyclerview.widget.LinearLayoutManager
// ספרייה ליצירת רשימות נגללות
import androidx.recyclerview.widget.RecyclerView
// Activity להוספת משימה
import com.example.taskmasterfinalproject.addtask.AddTaskActivity
// מחלקת ה-Binding של המסך הראשי
import com.example.taskmasterfinalproject.databinding.ActivityMainBinding
// Activity לפרטי משימה
import com.example.taskmasterfinalproject.details.TaskDetailActivity
// ה-ViewModel הראשי
import com.example.taskmasterfinalproject.main.MainViewModel
// המתאם של רשימת המשימות
import com.example.taskmasterfinalproject.main.TaskAdapter
// המודל המייצג משימה
import com.example.taskmasterfinalproject.model.Task
// מחלקה לניהול התראות והודעות
import com.example.taskmasterfinalproject.notifications.NotificationHelper
// מחלקה לתזמון תזכורות
import com.example.taskmasterfinalproject.notifications.ReminderScheduler
// רכיב להצגת הודעות קצרות (Snackbar)
import com.google.android.material.snackbar.Snackbar

// ה-Activity הראשי של האפליקציה, משמש כנקודת הכניסה ומנהל את הניווט הראשי
class MainActivity : AppCompatActivity() {

    // binding – אובייקט ViewBinding שמאפשר גישה ישירה לרכיבי ה-UI ללא findViewById
    private lateinit var binding: ActivityMainBinding
    // ViewModel – מנהל את הנתונים ושורד סיבוב מסך. by viewModels() = האצלה (delegation)
    private val viewModel: MainViewModel by viewModels()

    // פונקציית ה-Lifecycle הראשית, נקראת ביצירת המסך
    override fun onCreate(savedInstanceState: Bundle?) {
        // החלת ערכת הנושא לפני super.onCreate() – חייב להיות לפני setContentView
        com.example.taskmasterfinalproject.settings.ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)  // קריאה ל-super – חובה בכל Lifecycle callback
        
        // בדיקת הפעלה (Session)
        // עדיפות: בזיכרון (רק התחבר) -> מתמיד (זכור אותי)
        // אם בזיכרון מוגדר, אנחנו בסדר.
        if (!com.example.taskmasterfinalproject.auth.SessionManager.isLoggedIn()) {
             // לא בזיכרון, בדוק העדפות
             val prefs = com.example.taskmasterfinalproject.data.PreferencesManager(this)
             val savedId = prefs.getSessionUserId()
             if (savedId != null) {
                 // נמצא session שמור ותקין, טען לזיכרון
                 com.example.taskmasterfinalproject.auth.SessionManager.startSession(savedId)
             } else {
                 // אין session באף מקום
                 startActivity(Intent(this, com.example.taskmasterfinalproject.auth.LoginActivity::class.java))
                 finish()
                 return
             }
        }
        
        // אתחול ה-ViewBinding – inflate יוצר את מעץ ה-Views מה-XML
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)  // הגדרת ה-Root View כתוכן המסך
        setSupportActionBar(binding.toolbar) // הגדרת ה-Toolbar כסרגל כלים ראשי (תפריט + כותרת)

        // טיפול ב-WindowInsets – התאמת הריפוד (padding) לסרגלי המערכת (סטטוס בר, ניווט)
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)  // לא מוסיפים ריפוד למטה, BottomNav מטפל
            insets  // מחזיר את ה-insets לשימוש רכיבים אחרים
        }

        // יצירת ערוצי התראות (Notification Channels) – נדרש פעם אחת בחיי האפליקציה
        NotificationHelper.createTaskReminderChannels(this)
        // בקשת הרשאת התראות (נדרש באנדרואיד 13 ומעלה)
        requestNotificationPermissionIfNeeded()
        // בקשת הרשאת התראות מדויקות (Exact Alarms)
        ReminderScheduler.requestExactAlarmPermissionIfNeeded(this)

        // טעינת הפרגמנט הראשוני רק אם זו יצירה חדשה (לא סיבוב מסך)
        if (savedInstanceState == null) {
            // FragmentTransaction – מחליף את הפרגמנט ב-Container שמוגדר ב-XML
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, com.example.taskmasterfinalproject.main.TaskListFragment())
                .commit()  // commit() – ביצוע הטרנזקציה
        }

        // מאזין לניווט התחתון (Bottom Navigation) – מחליף פרגמנטים לפי בחירת הטאב
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_tasks -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, com.example.taskmasterfinalproject.main.TaskListFragment())
                        .commit()
                    true
                }
                R.id.nav_statistics -> {
                    // נווט לפרגמנט סטטיסטיקה
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, com.example.taskmasterfinalproject.statistics.StatisticsFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

    // פונקציה לבקשת הרשאת התראות (בוצע רק עבור אנדרואיד 13 ומעלה)
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    // יצירת תפריט האפשרויות (Settings, Profile)
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        // inflate – טוען את קובץ ה-XML של התפריט לתוך ה-Menu
        menuInflater.inflate(R.menu.menu_main, menu)
        return true  // true = התפריט יוצג
    }

    // טיפול בבחירת פריט מתפריט האפשרויות
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                com.example.taskmasterfinalproject.settings.SettingsBottomSheet.newInstance()
                    .show(supportFragmentManager, com.example.taskmasterfinalproject.settings.SettingsBottomSheet.TAG)
                true
            }
            R.id.action_profile -> {
                startActivity(Intent(this, com.example.taskmasterfinalproject.profile.ProfileActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
