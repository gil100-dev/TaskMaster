package com.example.taskmasterfinalproject.ai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class AiViewModel : ViewModel() {

    private val _aiResponse = MutableLiveData<String>()
    val aiResponse: LiveData<String> = _aiResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _subtaskPlan = MutableLiveData<AiSubtaskPlan?>()
    val subtaskPlan: LiveData<AiSubtaskPlan?> = _subtaskPlan

    // NOTE: In production, use BuildConfig.API_KEY
    private val apiKey = "AIzaSyBge1chu-qPZ4z2OaHBA9CCN0ZzO3bAk7s"
    private val repository = AiRepository()

    fun clearPlan() {
        _subtaskPlan.value = null
    }

    fun setCachedResponse(response: String) {
        _aiResponse.value = response
    }

    fun generateAdvice(taskTitle: String, taskDescription: String?, priority: Int, subtasks: List<String> = emptyList()) {
        if (taskTitle.trim().split("\\s+".toRegex()).size < 2 && taskDescription.isNullOrBlank()) {
             _aiResponse.value = "המשימה קצת כללית מדי. תוכל להוסיף פירוט?"
            return
        }

        _isLoading.value = true
        _aiResponse.value = "מתחבר ל-AI..." 
        _subtaskPlan.value = null

        viewModelScope.launch {
            try {
                // 1. Discovery & Selection
                val availableModels = repository.getAvailableModels(apiKey)
                val selectedModelName = when {
                    availableModels.contains("gemini-1.5-flash") -> "gemini-1.5-flash"
                    availableModels.contains("gemini-1.5-pro") -> "gemini-1.5-pro"
                    availableModels.contains("gemini-pro") -> "gemini-pro"
                    availableModels.isNotEmpty() -> availableModels.first()
                    else -> null
                }

                if (selectedModelName == null) {
                    _aiResponse.value = "שגיאת AI: אין מודל זמין."
                    return@launch
                }

                _aiResponse.value = "חושב ($selectedModelName)..."
                
                val generativeModel = GenerativeModel(selectedModelName, apiKey)

                val priorityStr = when(priority) {
                    1 -> "High (דחוף)"
                    2 -> "Medium (רגיל)"
                    else -> "Low (נמוך)"
                }
                
                val subtasksStr = if (subtasks.isNotEmpty()) "\nExisting Subtasks:\n${subtasks.joinToString("\n") { "- $it" }}" else ""
                
                val prompt = """
                    You are a task management coach speaking in HEBREW.
                    Analyze this specific task:
                    Title: "$taskTitle"
                    Description: "${taskDescription ?: "None"}"
                    Priority: $priorityStr
                    $subtasksStr
                    
                    Give advice ONLY for THIS task. Do not give general productivity tips.
                    Response MUST be in HEBREW.
                    
                    STRICT OUTPUT FORMAT (Use HTML <b> tags for headings):
                    
                    <b>סיכום קצר</b>
                    (משפט אחד)
                    
                    <b>צעדי ביצוע</b>
                    (5-7 bullet points, concrete steps)
                    
                    <b>הערכת זמן</b>
                    (Total range + breakdown per step)
                    
                    <b>מתי הכי כדאי לבצע</b>
                    (Morning/Afternoon/Evening + short reason)
                    
                    <b>משפט עידוד</b>
                    (Short and motivating)
                    
                    <b>שאלה מבהירה</b>
                    (One short clarifying question if needed, otherwise omit)
                    
                    Max 250 words. Use simple HTML tags <b> and <br>.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                _aiResponse.value = response.text ?: "לא הצלחתי לייצר עצה כרגע."
                
            } catch (e: Exception) {
               _aiResponse.value = "שגיאה: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateSubtasksPlan(taskTitle: String, taskDescription: String?, priority: Int) {
         _isLoading.value = true
         _aiResponse.value = "Generating Plan..."
         _subtaskPlan.value = null

         viewModelScope.launch {
             try {
                // Ensure model is selected
                 val availableModels = repository.getAvailableModels(apiKey)
                 val selectedModelName = when {
                    availableModels.contains("gemini-1.5-flash") -> "gemini-1.5-flash"
                    availableModels.contains("gemini-1.5-pro") -> "gemini-1.5-pro"
                    availableModels.contains("gemini-pro") -> "gemini-pro"
                    availableModels.isNotEmpty() -> availableModels.first()
                    else -> null
                }
                
                if (selectedModelName == null) {
                    _aiResponse.value = "AI Error: No models available."
                    return@launch
                }

                val model = GenerativeModel(selectedModelName, apiKey)
                val priorityStr = when(priority) { 1 -> "High" 2 -> "Medium" else -> "Low" }
                
                val result = repository.generateSubtasks(model, taskTitle, taskDescription, priorityStr)
                
                result.fold(
                    onSuccess = { plan ->
                        _subtaskPlan.value = plan
                        _aiResponse.value = "Plan Ready! Preview below."
                    },
                    onFailure = { e ->
                        _aiResponse.value = "Couldn't parse AI output, try again. (${e.message})"
                    }
                )
             } catch (e: Exception) {
                 _aiResponse.value = "Error: ${e.localizedMessage}"
             } finally {
                 _isLoading.value = false
             }
         }
    }
}
