package com.example.taskmasterfinalproject.ai

// אובייקט LiveData לקריאה בלבד
import androidx.lifecycle.LiveData
// אובייקט LiveData ניתן לשינוי
import androidx.lifecycle.MutableLiveData
// מחלקת הבסיס ל-ViewModel
import androidx.lifecycle.ViewModel
// גישה ל-Scope של הקורוטינות ב-ViewModel
import androidx.lifecycle.viewModelScope
// המודל הגנרטיבי של גוגל
import com.google.ai.client.generativeai.GenerativeModel
// הפעלת קורוטינה
import kotlinx.coroutines.launch

// ViewModel המנהל את הלוגיקה והמצב של מסך ה-AI
class AiViewModel : ViewModel() {

    // דפוס Encapsulation: MutableLiveData פרטי (ניתן לשינוי מתוך ה-ViewModel בלבד)
    // LiveData ציבורי (לקריאה בלבד מה-UI) – מונע שינוי ישיר מצד ה-View
    private val _aiResponse = MutableLiveData<String>()
    val aiResponse: LiveData<String> = _aiResponse       // תשובת ה-AI המוצגת למשתמש

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading         // מצב טעינה (להצגת ProgressBar)

    private val _subtaskPlan = MutableLiveData<AiSubtaskPlan?>()
    val subtaskPlan: LiveData<AiSubtaskPlan?> = _subtaskPlan  // תוכנית תת-משימות שנוצרה ע"י ה-AI

    // מפתח API לגישה ל-Gemini (בפרודקשן משתמשים ב-BuildConfig.API_KEY ולא hardcoded)
    private val apiKey = "AIzaSyCkq-Zw6SZNAFuybsmxuJgxJqcCufrzGbw"
    // המאגר שמטפל בתקשורת בפועל מול ה-API
    private val repository = AiRepository()

    // איפוס התוכנית הנוכחית
    fun clearPlan() {
        _subtaskPlan.value = null
    }

    // טעינת תשובה שמורה מהמטמון
    fun setCachedResponse(response: String) {
        _aiResponse.value = response
    }

    // הפעלת תהליך קבלת עצה מה-AI
    fun generateAdvice(taskTitle: String, taskDescription: String?, priority: Int, subtasks: List<String> = emptyList()) {
        // ולידציה: אם הכותרת פחות מ-2 מילים ואין תיאור – המשימה כללית מדי ל-AI
        if (taskTitle.trim().split("\\s+".toRegex()).size < 2 && taskDescription.isNullOrBlank()) {
             _aiResponse.value = "המשימה קצת כללית מדי. תוכל להוסיף פירוט?"
            return
        }

        _isLoading.value = true
        _aiResponse.value = "מתחבר ל-AI..." 
        _subtaskPlan.value = null

        // viewModelScope – CoroutineScope שמתבטל אוטומטית כש-ViewModel נהרס (מונע דליפות זיכרון)
        viewModelScope.launch {
            try {
                // שלב 1: גילוי (Discovery) – שליפת רשימת המודלים הזמינים מה-API
                val availableModels = repository.getAvailableModels(apiKey)
                // שלב 2: בחירת מודל לפי סדר עדיפות (המהיר ביותר קודם)
                val selectedModelName = when {
                    availableModels.contains("gemini-2.5-flash") -> "gemini-2.5-flash"
                    availableModels.contains("gemini-1.5-flash") -> "gemini-1.5-flash"
                    availableModels.contains("gemini-1.5-pro") -> "gemini-1.5-pro"
                    availableModels.contains("gemini-2.0-flash") -> "gemini-2.0-flash"
                    availableModels.isNotEmpty() -> availableModels.first()  // fallback לכל מודל שזמין
                    else -> null  // אין מודלים כלל
                }

                if (selectedModelName == null) {
                    _aiResponse.value = "שגיאת AI: אין מודל זמין."
                    return@launch  // return@launch – יציאה מהקורוטינה (לא מהפונקציה)
                }

                _aiResponse.value = "חושב ($selectedModelName)..."
                
                // יצירת מופע של המודל הגנרטיבי עם המודל שנבחר ומפתח ה-API
                val generativeModel = GenerativeModel(selectedModelName, apiKey)

                // המרת ערך העדיפות הספרתי למחרוזת מובנת ל-AI (Prompt Engineering)
                val priorityStr = when(priority) {
                    3 -> "High (דחוף)"
                    2 -> "Medium (רגיל)"
                    else -> "Low (נמוך)"
                }
                
                // בניית מחרוזת תת-המשימות הקיימות (אם יש) לשילוב בפרומפט
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

                // שליחת הפרומפט למודל וקבלת תשובה
                val response = generativeModel.generateContent(prompt)
                // עדכון ה-LiveData בתשובה – ה-UI יתעדכן אוטומטית (דפוס Observer)
                _aiResponse.value = response.text ?: "לא הצלחתי לייצר עצה כרגע."
                
            } catch (e: Exception) {
               _aiResponse.value = "שגיאה: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // הפעלת תהליך יצירת תוכנית עבודה מה-AI
    fun generateSubtasksPlan(taskTitle: String, taskDescription: String?, priority: Int) {
         _isLoading.value = true
         _aiResponse.value = "Generating Plan..."
         _subtaskPlan.value = null

         viewModelScope.launch {
             try {
                // Ensure model is selected
                 val availableModels = repository.getAvailableModels(apiKey)
                 val selectedModelName = when {
                    availableModels.contains("gemini-2.5-flash") -> "gemini-2.5-flash"
                    availableModels.contains("gemini-1.5-flash") -> "gemini-1.5-flash"
                    availableModels.contains("gemini-1.5-pro") -> "gemini-1.5-pro"
                    availableModels.contains("gemini-2.0-flash") -> "gemini-2.0-flash"
                    availableModels.isNotEmpty() -> availableModels.first()
                    else -> null
                }
                
                if (selectedModelName == null) {
                    _aiResponse.value = "AI Error: No models available."
                    return@launch
                }

                val model = GenerativeModel(selectedModelName, apiKey)
                val priorityStr = when(priority) { 3 -> "High" 2 -> "Medium" else -> "Low" }
                
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
