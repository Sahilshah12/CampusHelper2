package com.campushelper.app.data.repository

import com.campushelper.app.data.model.Material
import com.campushelper.app.data.remote.ApiService
import com.campushelper.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MaterialRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getMaterials(): Resource<List<Material>> {
        return withContext(Dispatchers.IO) {
            try {
                // Get all materials by passing empty string
                val response = apiService.getMaterials("")
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.materials)
                } else {
                    Resource.Error(response.message() ?: "Failed to fetch materials")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getMaterialsBySubject(subjectId: String): Resource<List<Material>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMaterials(subjectId)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.materials)
                } else {
                    Resource.Error(response.message() ?: "Failed to fetch materials")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }
}
