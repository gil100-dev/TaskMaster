package com.example.taskmasterfinalproject.audio

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

/**
 * Manages priority-based sound feedback using ToneGenerator.
 * This ensures audio feedback without requiring external assets (mp3/ogg).
 */
object SoundManager {

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)

    fun playPrioritySound(priority: Int) {
        try {
            val toneType = when (priority) {
                1 -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD // High Priority (Urgent)
                2 -> ToneGenerator.TONE_PROP_PROMPT          // Medium Priority (Neutral)
                3 -> ToneGenerator.TONE_CDMA_ANSWER          // Low Priority (Subtle)
                else -> ToneGenerator.TONE_PROP_BEEP
            }
            toneGenerator.startTone(toneType, 200) // Play for 200ms
        } catch (e: Exception) {
            Log.e("SoundManager", "Error playing sound: ${e.message}")
        }
    }

    fun release() {
        toneGenerator.release()
    }
}
