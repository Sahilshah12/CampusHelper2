package com.campushelper.app.data.model

data class CompetitiveExam(
    val _id: String,
    val name: String,
    val shortName: String,
    val description: String?,
    val category: String,
    val subjects: List<String>
)

data class CompetitiveExamsResponse(
    val success: Boolean,
    val count: Int,
    val exams: List<CompetitiveExam>
)

data class EnrollRequest(
    val dailyTestsCount: Int
)

data class EnrollResponse(
    val success: Boolean,
    val message: String
)
