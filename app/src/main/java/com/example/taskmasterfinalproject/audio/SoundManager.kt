package com.example.taskmasterfinalproject.audio

// ספרייה לניהול שמע במערכת
import android.media.AudioManager
// ספרייה ליצירת צלילים מערכתיים פשוטים (DTMF tones וכו')
import android.media.ToneGenerator
// ספרייה לרישום לוגים
import android.util.Log

/**
 * אובייקט לניהול משוב קולי מבוסס עדיפות באמצעות ToneGenerator.
 * מבטיח משוב קולי ללא צורך בקבצי נכסים חיצוניים (MP3/OGG).
 */
object SoundManager {

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)

    // פונקציה לניגון צליל בהתאם לעדיפות המשימה
    fun playPrioritySound(priority: Int) {
        try {
            val toneType = when (priority) {
                3 -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD // עדיפות גבוהה (דחוף)
                2 -> ToneGenerator.TONE_PROP_PROMPT          // עדיפות בינונית (נייטרלי)
                1 -> ToneGenerator.TONE_CDMA_ANSWER          // עדיפות נמוכה (עדין)
                else -> ToneGenerator.TONE_PROP_BEEP
            }
            toneGenerator.startTone(toneType, 200) // ניגון למשך 200 מילישניות
        } catch (e: Exception) {
            Log.e("SoundManager", "Error playing sound: ${e.message}")
        }
    }

    // פונקציה לשחרור משאבים
    fun release() {
        toneGenerator.release()
    }
}
