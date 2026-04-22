package com.campushelper.app.data.repository

import com.campushelper.app.data.model.*
import com.campushelper.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CompetitiveExamRepository @Inject constructor() {

    suspend fun getExams(): Resource<List<CompetitiveExam>> {
        return withContext(Dispatchers.IO) {
            Resource.Error("Competitive exams are temporarily disabled during Firebase migration")
        }
    }

    suspend fun enrollInExam(examId: String, dailyTestsCount: Int): Resource<EnrollResponse> {
        return withContext(Dispatchers.IO) {
            Resource.Error("Competitive exams are temporarily disabled during Firebase migration")
        }
    }

    suspend fun generateCompetitiveTest(examId: String): Resource<Test> {
        return withContext(Dispatchers.IO) {
            Resource.Error("Competitive exams are temporarily disabled during Firebase migration")
        }
    }
}
