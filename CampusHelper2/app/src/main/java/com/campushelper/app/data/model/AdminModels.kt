package com.campushelper.app.data.model

// Subject Management
data class CreateSubjectRequest(
    val name: String,
    val code: String,
    val description: String,
    val category: String = "Engineering",
    val semester: Int = 1,
    val credits: Int = 3
)

data class UpdateSubjectRequest(
    val name: String,
    val code: String,
    val description: String,
    val category: String,
    val semester: Int,
    val credits: Int
)

data class SubjectResponse(
    val success: Boolean,
    val subject: Subject
)

// Material Management
data class CreateMaterialRequest(
    val subjectId: String,
    val title: String,
    val type: String, // pdf, video, link
    val description: String,
    val url: String,
    val fileSize: Long? = null
)

data class UpdateMaterialRequest(
    val title: String,
    val type: String,
    val description: String,
    val url: String,
    val fileSize: Long? = null
)

data class MaterialResponse(
    val success: Boolean,
    val material: Material
)

// Competitive Exam Management
data class CreateCompetitiveExamRequest(
    val name: String,
    val shortName: String,
    val description: String,
    val subjects: List<String>,
    val totalMarks: Int,
    val duration: Int, // in minutes
    val negativeMarking: Boolean = false,
    val examDate: String? = null,
    val registrationDeadline: String? = null,
    val eligibility: String? = null
)

data class UpdateCompetitiveExamRequest(
    val name: String,
    val shortName: String,
    val description: String,
    val subjects: List<String>,
    val totalMarks: Int,
    val duration: Int,
    val negativeMarking: Boolean,
    val examDate: String?,
    val registrationDeadline: String?,
    val eligibility: String?
)

data class CompetitiveExamResponse(
    val success: Boolean,
    val exam: CompetitiveExam
)

// Common Delete Response
data class DeleteResponse(
    val success: Boolean,
    val message: String
)
