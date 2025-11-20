package com.campushelper.app.data.repository

import com.campushelper.app.data.model.AiChatRequest
import com.campushelper.app.data.model.AiChatResponse
import com.campushelper.app.data.remote.ApiService
import com.campushelper.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AiRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun chat(subjectId: String, topic: String, question: String?): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = AiChatRequest(subjectId, topic, question)
                val response = apiService.aiChat(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    
                    // Check if the response is successful and has content
                    if (body.success && body.response.isNotBlank()) {
                        Resource.Success(body.response)
                    } else {
                        Resource.Error("AI returned an empty response. Please try again.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    
                    // Parse error message for better user feedback
                    val errorMessage = when {
                        errorBody?.contains("overloaded", ignoreCase = true) == true -> 
                            "AI service is busy. Please try again in a moment."
                        errorBody?.contains("quota", ignoreCase = true) == true -> 
                            "Daily AI usage limit reached. Try again tomorrow."
                        errorBody?.contains("API key", ignoreCase = true) == true -> 
                            "AI service configuration issue. Please contact support."
                        else -> errorBody ?: "Failed to get AI response. Please try again."
                    }
                    
                    Resource.Error(errorMessage)
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error. Please check your connection.")
            }
        }
    }
}
