package com.example.taskmasterfinalproject.ai

// ספרייה לרישום לוגים
import android.util.Log
// ספרייה להגדרת ה-Dispatchers של הקורוטינות
import kotlinx.coroutines.Dispatchers
// ספרייה להחלפת ה-Dispatcher הנוכחי
import kotlinx.coroutines.withContext
// ספרייה לקריאה ויצירה של אובייקטי JSON
import org.json.JSONObject
// ספרייה לביצוע בקשות HTTP
import java.net.HttpURLConnection
// ספרייה לייצוג כתובת URL
import java.net.URL

// מחלקה האחראית על התקשורת מול ה-API של Gemini
class AiRepository {

    // שליפת רשימת המודלים הזמינים מה-API
    // suspend – פונקציה שיכולה להשעות את הביצוע בלי לחסום את ה-Thread (Coroutine)
    // withContext(Dispatchers.IO) – מעביר את הביצוע ל-Thread Pool של IO (לא חוסם את ה-UI Thread)
    suspend fun getAvailableModels(apiKey: String): List<String> = withContext(Dispatchers.IO) {
        // בניית כתובת ה-API עם מפתח האימות כפרמטר
        val urlString = "https://generativelanguage.googleapis.com/v1beta/models?key=${apiKey}"
        val models = mutableListOf<String>()
        
        try {
            // פתיחת חיבור HTTP ל-REST API של Google
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"   // בקשת GET – שליפת נתונים בלבד
            
            if (connection.responseCode == 200) {  // 200 = OK – הבקשה הצליחה
                // קריאת גוף התשובה כטקסט (use סוגר את ה-stream אוטומטית)
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                // פענוח התשובה כאובייקט JSON
                val json = JSONObject(response)
                
                if (json.has("models")) {
                    val array = json.getJSONArray("models")
                    // מעבר על כל מודל ובדיקה אם תומך ב-generateContent
                    for (i in 0 until array.length()) {
                        val modelObj = array.getJSONObject(i)
                        val name = modelObj.getString("name") // e.g., "models/gemini-pro"
                        val methods = modelObj.optJSONArray("supportedGenerationMethods")
                        
                        var supportsGenerateContent = false
                        if (methods != null) {
                            for (j in 0 until methods.length()) {
                                if (methods.getString(j) == "generateContent") {
                                    supportsGenerateContent = true
                                    break
                                }
                            }
                        }
                        
                        if (supportsGenerateContent) {
                            models.add(name.replace("models/", ""))  // ניקוי הקידומת "models/" מהשם
                        }
                    }
                }
            } else {
                Log.e("AiRepository", "Error listing models: Code ${connection.responseCode}")
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e("AiRepository", "Error Body: $errorStream")
            }
        } catch (e: Exception) {
            Log.e("AiRepository", "Exception listing models", e)
        }
        
        Log.d("AiRepository", "Available Models: $models")
        return@withContext models  // return@withContext – החזרת ערך מתוך בלוק withContext
    }


    // שליחת בקשה ל-AI ליצירת תוכנית תתי-משימות
    suspend fun generateSubtasks(
        generativeModel: com.google.ai.client.generativeai.GenerativeModel,
        taskTitle: String,
        taskDescription: String?,
        priority: String
    ): Result<AiSubtaskPlan> {
        return withContext(Dispatchers.IO) {  // ביצוע ב-IO Thread – מתאים לפעולות רשת וקבצים
            try {
                val prompt = """
                    You are a task planner. Break down the task "$taskTitle" into 5-10 subtasks.
                    Description: ${taskDescription ?: "None"}
                    Priority: $priority
                    
                    Return ONLY valid JSON (no markdown formatting). Schema:
                    {
                      "subtasks": [
                        { "title": "String", "minutes": 15 }
                      ],
                      "total_minutes_range": "String",
                      "best_time_of_day": "Morning/Afternoon/Evening",
                      "reason": "String"
                    }
                """.trimIndent()

                // שליחת הפרומפט למודל ה-AI וקבלת תשובה
                val response = generativeModel.generateContent(prompt)
                val text = response.text ?: throw Exception("Empty AI response")
                
                // ניקוי תגיות Markdown שה-AI עלול להוסיף סביב ה-JSON
                val cleanJson = text.replace("```json", "").replace("```", "").trim()
                
                // פענוח ה-JSON שהתקבל מה-AI למבנה נתונים של Kotlin
                val jsonObj = JSONObject(cleanJson)
                val subtasksArray = jsonObj.getJSONArray("subtasks")
                val subtasksList = mutableListOf<AiSubtask>()
                
                // מעבר על כל תת-משימה ב-JSON והמרה לאובייקט AiSubtask
                for (i in 0 until subtasksArray.length()) {
                    val item = subtasksArray.getJSONObject(i)
                    subtasksList.add(AiSubtask(item.getString("title"), item.getInt("minutes")))
                }
                
                // בניית אובייקט התוכנית המלא
                val plan = AiSubtaskPlan(
                    subtasks = subtasksList,
                    totalMinutesRange = jsonObj.getString("total_minutes_range"),
                    bestTimeOfDay = jsonObj.getString("best_time_of_day"),
                    reason = jsonObj.getString("reason")
                )
                
                Result.success(plan)  // עטיפה ב-Result.success – דפוס לטיפול בהצלחה/כישלון
            } catch (e: Exception) {
                Log.e("AiRepository", "Error parsing plan", e)
                Result.failure(e)
            }
        }
    }
}
