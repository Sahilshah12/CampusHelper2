package com.campushelper.app.data.model

data class Question(
    val id: String,
    val question: String,
    val options: Map<String, String>, // {"A": "option1", "B": "option2", ...}
    val correctAnswer: String,
    val explanation: String? = null
)

data class TestResult(
    val testId: String,
    val subjectId: String,
    val score: Double,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val completedAt: Long,
    val questions: List<Question>? = null,
    val userAnswers: Map<String, String>? = null
)

data class SubmitTestResponse(
    val testId: String,
    val score: Double,
    val correctAnswers: Int,
    val incorrectAnswers: Int
)
