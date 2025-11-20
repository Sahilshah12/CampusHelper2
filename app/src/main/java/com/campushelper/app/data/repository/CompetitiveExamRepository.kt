package com.campushelper.app.data.repository

import com.campushelper.app.data.model.*
import com.campushelper.app.data.remote.ApiService
import com.campushelper.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CompetitiveExamRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getExams(): Resource<List<CompetitiveExam>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCompetitiveExams()
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.exams)
                } else {
                    Resource.Error(response.message() ?: "Failed to fetch exams")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun enrollInExam(examId: String, dailyTestsCount: Int): Resource<EnrollResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.enrollInExam(examId, EnrollRequest(dailyTestsCount))
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error(response.message() ?: "Failed to enroll")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun generateCompetitiveTest(examId: String): Resource<Test> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.generateCompetitiveTest(examId)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.test)
                } else {
                    Resource.Error(response.message() ?: "Failed to generate test")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }
}
