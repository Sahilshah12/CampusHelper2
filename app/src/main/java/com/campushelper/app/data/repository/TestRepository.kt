package com.campushelper.app.data.repository

import com.campushelper.app.data.model.*
import com.campushelper.app.data.remote.ApiService
import com.campushelper.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TestRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun generateTest(request: TestRequest): Resource<Test> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.generateTest(request)
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

    suspend fun getTests(status: String? = null, subjectId: String? = null): Resource<List<Test>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTests(status, subjectId)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.tests)
                } else {
                    Resource.Error(response.message() ?: "Failed to fetch tests")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun submitTest(testId: String, answers: List<Int>): Resource<TestResults> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.submitTest(testId, SubmitTestRequest(answers))
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.results)
                } else {
                    Resource.Error(response.message() ?: "Failed to submit test")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getTest(testId: String): Resource<Test> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTest(testId)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.test)
                } else {
                    Resource.Error(response.message() ?: "Failed to fetch test")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getTestResult(testId: String): Resource<TestResult> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTest(testId)
                if (response.isSuccessful && response.body() != null) {
                    val test = response.body()!!.test
                    // Convert Test to TestResult
                    val testResult = TestResult(
                        testId = test._id,
                        subjectId = test.userId, // TODO: This should map properly from backend
                        score = test.score,
                        totalQuestions = test.totalQuestions,
                        correctAnswers = test.correctAnswers,
                        incorrectAnswers = test.totalQuestions - test.correctAnswers,
                        completedAt = System.currentTimeMillis(),
                        questions = test.questions,
                        userAnswers = null // TODO: Map user answers if needed
                    )
                    Resource.Success(testResult)
                } else {
                    Resource.Error(response.message() ?: "Failed to fetch test result")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getTestAnalysis(testId: String): Resource<TestAnalysis> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTestAnalysis(testId)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.analysis)
                } else {
                    Resource.Error(response.message() ?: "Failed to fetch test analysis")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }
}
