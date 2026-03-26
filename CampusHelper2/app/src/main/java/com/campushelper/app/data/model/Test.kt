package com.campushelper.app.data.model

data class TestRequest(
    val subjectId: String,
    val topic: String,
    val questionCount: Int = 10,
    val difficulty: String = "medium"
)

data class Test(
    val _id: String,
    val userId: String,
    val title: String,
    val topic: String?,
    val questions: List<Question>,
    val userAnswers: List<Int>,
    val score: Double,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val status: String
)

data class TestResponse(
    val success: Boolean,
    val test: Test
)

data class SubmitTestRequest(
    val userAnswers: List<Int>
)

data class TestResultsResponse(
    val success: Boolean,
    val test: Test,
    val results: TestResults
)

data class TestResults(
    val score: Double,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val percentage: String
)

data class TestsResponse(
    val success: Boolean,
    val count: Int,
    val tests: List<Test>
)

// Test Analysis Models
data class TestAnalysis(
    val _id: String,
    val subjectName: String,
    val topic: String?,
    val scorePercentage: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val totalTimeSeconds: Int,
    val difficultyBreakdown: DifficultyBreakdown?,
    val aiAnalysis: AIAnalysis?,
    val questions: List<QuestionReview>
)

data class DifficultyBreakdown(
    val easy: DifficultyStats,
    val medium: DifficultyStats,
    val hard: DifficultyStats
)

data class DifficultyStats(
    val correct: Int,
    val total: Int
)

data class AIAnalysis(
    val overallAssessment: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val recommendations: List<String>,
    val studyPlan: List<String>
)

data class QuestionReview(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val userAnswer: Int?,
    val difficulty: String?
)

data class TestAnalysisResponse(
    val success: Boolean,
    val analysis: TestAnalysis
)
