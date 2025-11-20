package com.campushelper.app.data.repository

import com.campushelper.app.data.model.Material
import com.campushelper.app.data.model.Subject
import com.campushelper.app.data.remote.ApiService
import com.campushelper.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SubjectRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getSubjects(): Resource<List<Subject>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSubjects()
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.subjects)
                } else {
                    Resource.Error(response.message() ?: "Failed to fetch subjects")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getSubject(id: String): Resource<Subject> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSubject(id)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error(response.message() ?: "Failed to fetch subject")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getMaterials(subjectId: String): Resource<List<Material>> {
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
