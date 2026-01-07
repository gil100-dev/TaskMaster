package com.example.taskmasterfinalproject.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AiRepository {

    suspend fun getAvailableModels(apiKey: String): List<String> = withContext(Dispatchers.IO) {
        val urlString = "https://generativelanguage.googleapis.com/v1beta/models?key=${apiKey}"
        val models = mutableListOf<String>()
        
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                if (json.has("models")) {
                    val array = json.getJSONArray("models")
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
                            models.add(name.replace("models/", "")) // Cleanup name
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
        return@withContext models
    }


    suspend fun generateSubtasks(
        generativeModel: com.google.ai.client.generativeai.GenerativeModel,
        taskTitle: String,
        taskDescription: String?,
        priority: String
    ): Result<AiSubtaskPlan> {
        return withContext(Dispatchers.IO) {
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

                val response = generativeModel.generateContent(prompt)
                val text = response.text ?: throw Exception("Empty AI response")
                
                // Cleanup potentially markdown-wrapped JSON
                val cleanJson = text.replace("```json", "").replace("```", "").trim()
                
                val jsonObj = JSONObject(cleanJson)
                val subtasksArray = jsonObj.getJSONArray("subtasks")
                val subtasksList = mutableListOf<AiSubtask>()
                
                for (i in 0 until subtasksArray.length()) {
                    val item = subtasksArray.getJSONObject(i)
                    subtasksList.add(AiSubtask(item.getString("title"), item.getInt("minutes")))
                }
                
                val plan = AiSubtaskPlan(
                    subtasks = subtasksList,
                    totalMinutesRange = jsonObj.getString("total_minutes_range"),
                    bestTimeOfDay = jsonObj.getString("best_time_of_day"),
                    reason = jsonObj.getString("reason")
                )
                
                Result.success(plan)
            } catch (e: Exception) {
                Log.e("AiRepository", "Error parsing plan", e)
                Result.failure(e)
            }
        }
    }
}
