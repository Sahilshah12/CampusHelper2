package com.campushelper.app.data.repository

import com.campushelper.app.data.model.Progress
import com.campushelper.app.data.remote.ApiService
import com.campushelper.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProgressRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getProgress(): Resource<Progress> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getProgress()
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.progress)
                } else {
                    Resource.Error(response.message() ?: "Failed to fetch progress")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }
}
