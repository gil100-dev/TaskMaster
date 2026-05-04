package com.example.taskmasterfinalproject.audio

// ספרייה לגישה להקשר (Context) של האפליקציה
import android.content.Context
// ספרייה למנוע טקסט-לדיבור של אנדרואיד
import android.speech.tts.TextToSpeech
// ספרייה לרישום לוגים
import android.util.Log
// ספרייה להגדרת שפה ואזור
import java.util.Locale

/**
 * מנהל טקסט-לדיבור (TTS) – מספק API פשוט להקראת טקסט.
 * מטפל באתחול המנוע, בחירת שפה (עדיפות לעברית), ושחרור משאבים.
 */
// מחלקה לניהול מנוע הטקסט-לדיבור
class TextToSpeechManager(context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "TextToSpeechManager"
    }

    // מופע של מנוע הטקסט-לדיבור
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    // דגל המציין אם המנוע אותחל בהצלחה
    private var isInitialized = false

    // פונקציה הנקראת כאשר מנוע ה-TTS מוכן לשימוש
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // ניסיון להגדיר עברית כשפה מועדפת
            val hebrewLocale = Locale("iw")
            val hebrewResult = tts?.isLanguageAvailable(hebrewLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED

            if (hebrewResult >= TextToSpeech.LANG_AVAILABLE) {
                tts?.language = hebrewLocale
                Log.d(TAG, "TTS initialized with Hebrew")
            } else {
                // נפילה לשפת ברירת המחדל של המכשיר
                val defaultLocale = Locale.getDefault()
                val defaultResult = tts?.isLanguageAvailable(defaultLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
                if (defaultResult >= TextToSpeech.LANG_AVAILABLE) {
                    tts?.language = defaultLocale
                    Log.d(TAG, "TTS initialized with device default: ${defaultLocale.displayLanguage}")
                } else {
                    // נפילה לאנגלית כברירת מחדל אחרונה
                    tts?.language = Locale.US
                    Log.d(TAG, "TTS initialized with English (US) as fallback")
                }
            }
            isInitialized = true
        } else {
            Log.e(TAG, "TTS initialization failed with status: $status")
            isInitialized = false
        }
    }

    // פונקציה להקראת טקסט בקול – עוצרת דיבור קודם ומתחילה חדש
    fun speak(text: String) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized yet, ignoring speak request")
            return
        }
        // עצירת דיבור נוכחי (מונע חפיפה) והתחלת הקראה חדשה
        tts?.stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_utterance_${System.currentTimeMillis()}")
    }

    // פונקציה לעצירת הדיבור הנוכחי
    fun stop() {
        tts?.stop()
    }

    // פונקציה לשחרור משאבי המנוע – יש לקרוא ב-onDestroy
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        Log.d(TAG, "TTS shutdown complete")
    }
}
