package com.example.taskmasterfinalproject.settings

// ספרייה להעברת נתונים בין רכיבים (Bundle)
import android.os.Bundle
// ספרייה לניפוח (Inflate) קבצי XML לתצוגה
import android.view.LayoutInflater
// מחלקת הבסיס לתצוגה
import android.view.View
// ספרייה המייצגת קבוצת Views
import android.view.ViewGroup
// ספרייה המאפשרת שליטה על מצבי הלילה/יום (DayNight) של האפליקציה
import androidx.appcompat.app.AppCompatDelegate
// משאבי האפליקציה
import com.example.taskmasterfinalproject.R
// מחלקה לניהול העדפות המשתמש (SharedPreferences)
import com.example.taskmasterfinalproject.data.PreferencesManager
// מחלקת ה-Binding של מסך ההגדרות (נוצרת אוטומטית מקובץ ה-XML)
import com.example.taskmasterfinalproject.databinding.FragmentSettingsBottomSheetBinding
// מחלקת בסיס לחלונית תחתונה (Bottom Sheet) של Material Design
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// חלונית תחתונה להגדרות האפליקציה (ערכת נושא וצבע דגש), יורשת מ-BottomSheetDialogFragment
class SettingsBottomSheet : BottomSheetDialogFragment() {

    // משתנה ה-Binding, nullable כדי למנוע דליפות זיכרון – מתאפס ב-onDestroyView
    private var _binding: FragmentSettingsBottomSheetBinding? = null
    // גישה נוחה ל-Binding (עם !! כי נשתמש בו רק כשהתצוגה קיימת)
    private val binding get() = _binding!!
    // מופע של מנהל ההעדפות לשמירה/קריאה של הגדרות המשתמש
    private lateinit var prefs: PreferencesManager

    // יצירת התצוגה – ניפוח ה-XML והחזרת ה-Root View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    // הגדרת לוגיקה ומאזינים לאחר שהתצוגה נוצרה
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // אתחול מנהל ההעדפות עם ה-Context הנוכחי
        prefs = PreferencesManager(requireContext())

        // הגדרת מצב התחלתי של כפתורי ערכת הנושא – מסנכרן עם מה ששמור ב-SharedPreferences
        when (prefs.getThemeMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.radioLight.isChecked = true      // מצב יום (בהיר)
            AppCompatDelegate.MODE_NIGHT_YES -> binding.radioDark.isChecked = true      // מצב לילה (כהה)
            else -> binding.radioSystem.isChecked = true                                 // לפי הגדרות המערכת
        }

        // הגדרת מצב התחלתי של צבע הדגש (Accent Color) – מסנכרן עם מה ששמור
        when (prefs.getAccentColor()) {
            "Green" -> binding.chipGreen.isChecked = true
            "Orange" -> binding.chipOrange.isChecked = true
            "Purple" -> binding.chipPurple.isChecked = true
            else -> binding.chipIndigo.isChecked = true   // ברירת מחדל: אינדיגו
        }

        // מאזין לשינוי ערכת הנושא (יום/לילה/מערכת)
        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            // מיפוי מזהה הכפתור הנבחר למצב ה-Night Mode המתאים
            val mode = when (checkedId) {
                R.id.radioLight -> AppCompatDelegate.MODE_NIGHT_NO              // מצב יום
                R.id.radioDark -> AppCompatDelegate.MODE_NIGHT_YES              // מצב לילה
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM              // לפי המערכת
            }
            // עדכון רק אם המצב השתנה (מניעת לולאה אינסופית)
            if (prefs.getThemeMode() != mode) {
                prefs.saveThemeMode(mode)                                        // שמירה ב-SharedPreferences
                AppCompatDelegate.setDefaultNightMode(mode)                      // החלת ערכת הנושא – גורם בדרך כלל ליצירה מחדש של ה-Activity
                dismiss()                                                        // סגירת החלונית לאפשר מעבר חלק
            }
        }

        // מאזין לשינוי צבע הדגש (Accent Color) – Chip Group
        binding.chipGroupColors.setOnCheckedChangeListener { group, checkedId ->
            // מיפוי ה-Chip הנבחר לשם הצבע
            val color = when (checkedId) {
                R.id.chipGreen -> "Green"
                R.id.chipOrange -> "Orange"
                R.id.chipPurple -> "Purple"
                else -> "Indigo"    // ברירת מחדל
            }

            // עדכון רק אם הצבע השתנה
            if (prefs.getAccentColor() != color) {
                prefs.saveAccentColor(color)                                     // שמירה ב-SharedPreferences
                requireActivity().recreate()                                     // יצירה מחדש של ה-Activity כדי להחיל את הסגנון החדש
                dismiss()                                                        // סגירת החלונית
            }
        }

        // כפתור סגירת חלונית ההגדרות
        binding.btnCloseSettings.setOnClickListener {
            dismiss()
        }
    }

    // ניקוי הפניות ל-Binding כדי למנוע דליפות זיכרון כשהתצוגה נהרסת
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // אובייקט נלווה (Companion Object) – מכיל קבועים ו-Factory Method
    companion object {
        // תגית ייחודית לזיהוי הפרגמנט ב-FragmentManager
        const val TAG = "SettingsBottomSheet"
        // פונקציית מפעל ליצירת מופע חדש של החלונית
        fun newInstance() = SettingsBottomSheet()
    }
}
