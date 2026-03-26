package com.campushelper.app.data.repository

import com.campushelper.app.data.model.*
import com.campushelper.app.data.remote.ApiService
import com.campushelper.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class AdminRepository @Inject constructor(
    private val apiService: ApiService
) {

    // Subject Management
    suspend fun createSubject(request: CreateSubjectRequest): Resource<Subject> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createSubject(request)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.subject)
                } else {
                    Resource.Error(response.message() ?: "Failed to create subject")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun updateSubject(id: String, request: UpdateSubjectRequest): Resource<Subject> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateSubject(id, request)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.subject)
                } else {
                    Resource.Error(response.message() ?: "Failed to update subject")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun deleteSubject(id: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteSubject(id)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.message)
                } else {
                    Resource.Error(response.message() ?: "Failed to delete subject")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    // Material Management
    suspend fun createMaterial(request: CreateMaterialRequest): Resource<Material> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createMaterial(request)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.material)
                } else {
                    Resource.Error(response.message() ?: "Failed to create material")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun updateMaterial(id: String, request: UpdateMaterialRequest): Resource<Material> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateMaterial(id, request)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.material)
                } else {
                    Resource.Error(response.message() ?: "Failed to update material")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun deleteMaterial(id: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteMaterial(id)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.message)
                } else {
                    Resource.Error(response.message() ?: "Failed to delete material")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    // Competitive Exam Management
    suspend fun createCompetitiveExam(request: CreateCompetitiveExamRequest): Resource<CompetitiveExam> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createCompetitiveExam(request)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.exam)
                } else {
                    Resource.Error(response.message() ?: "Failed to create exam")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun updateCompetitiveExam(id: String, request: UpdateCompetitiveExamRequest): Resource<CompetitiveExam> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateCompetitiveExam(id, request)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.exam)
                } else {
                    Resource.Error(response.message() ?: "Failed to update exam")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun deleteCompetitiveExam(id: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteCompetitiveExam(id)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.message)
                } else {
                    Resource.Error(response.message() ?: "Failed to delete exam")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    // Upload Material with File
    suspend fun uploadMaterial(
        subjectId: RequestBody,
        title: RequestBody,
        description: RequestBody,
        type: RequestBody,
        file: MultipartBody.Part? = null,
        url: RequestBody? = null
    ): Resource<Material> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.uploadMaterial(subjectId, title, description, type, file, url)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.material)
                } else {
                    Resource.Error(response.message() ?: "Failed to upload material")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred: ${e.localizedMessage}")
            }
        }
    }
}
