package com.example.taskmasterfinalproject.audio

// ספרייה המייצגת Activity
import android.app.Activity
// ספרייה ליצירת Intents
import android.content.Intent
// ספרייה לזיהוי דיבור של אנדרואיד
import android.speech.RecognizerIntent
// ספרייה לרישום לוגים
import android.util.Log
// ספרייה להגדרת שפה ואזור
import java.util.Locale

/**
 * מנהל קלט קולי (Speech-To-Text) – מספק API פשוט לזיהוי דיבור.
 * משתמש ב-RecognizerIntent להפעלת מנוע הזיהוי המובנה באנדרואיד.
 */
// מחלקה לניהול קלט קולי (דיבור-לטקסט)
object SpeechToTextManager {

    private const val TAG = "SpeechToTextManager"

    // קוד ייחודי לזיהוי תוצאת הקלט הקולי
    const val REQUEST_CODE_VOICE_INPUT = 9001

    /**
     * יוצר Intent לזיהוי דיבור – מעדיף עברית אם זמינה.
     * יש להשתמש ב-ActivityResultLauncher להפעלתו.
     */
    // פונקציה ליצירת Intent לזיהוי דיבור
    fun createVoiceInputIntent(): Intent {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        // הגדרת מודל שפה חופשי
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        // העדפת עברית, עם נפילה לשפת ברירת המחדל
        val hebrewLocale = Locale("iw")
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, hebrewLocale.toString())
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, hebrewLocale.toString())

        // טקסט מרמז למשתמש
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "דבר את המשימה שלך...")
        return intent
    }

    /**
     * מחלץ את הטקסט שזוהה מתוצאת ה-Intent.
     * מחזיר null אם לא זוהה טקסט.
     */
    // פונקציה לחילוץ הטקסט המזוהה מתוצאת ה-Intent
    fun extractRecognizedText(data: Intent?): String? {
        val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val text = results?.firstOrNull()
        if (text != null) {
            Log.d(TAG, "Recognized text: $text")
        } else {
            Log.w(TAG, "No text recognized from speech input")
        }
        return text
    }

    /**
     * מפצל טקסט ארוך לכותרת ותיאור בצורה חכמה.
     * אם הטקסט מכיל יותר מ-5 מילים, החלק הראשון הופך לכותרת והשאר לתיאור.
     * אחרת, הכל הופך לכותרת.
     *
     * @return Pair של (כותרת, תיאור) – התיאור יכול להיות null
     */
    // פונקציה לפיצול חכם של טקסט מזוהה לכותרת ותיאור
    fun splitToTitleAndDescription(text: String): Pair<String, String?> {
        val words = text.trim().split("\\s+".toRegex())

        return if (words.size > 5) {
            // חלוקה: 5 מילים ראשונות → כותרת, השאר → תיאור
            val title = words.take(5).joinToString(" ")
            val description = words.drop(5).joinToString(" ")
            Pair(title, description)
        } else {
            // הכל כותרת, אין תיאור
            Pair(text.trim(), null)
        }
    }
}
