package com.campushelper.app.data.model

data class Progress(
    val totalTests: Int,
    val totalQuestionsAttempted: Int,
    val totalCorrectAnswers: Int,
    val averageScore: Double,
    val subjectWiseProgress: List<SubjectProgress>,
    val streak: Streak
)

data class SubjectProgress(
    val subjectId: SubjectInfo,
    val testsAttempted: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val accuracy: Double
)

data class SubjectInfo(
    val _id: String,
    val name: String,
    val code: String
)

data class Streak(
    val current: Int,
    val longest: Int,
    val lastActivity: String?
)

data class ProgressResponse(
    val success: Boolean,
    val progress: Progress
)
