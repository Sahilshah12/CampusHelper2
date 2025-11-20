package com.campushelper.app.data.model

data class Subject(
    val _id: String,
    val name: String,
    val code: String,
    val description: String?,
    val category: String,
    val semester: Int,
    val credits: Int
)

data class SubjectsResponse(
    val success: Boolean,
    val count: Int,
    val subjects: List<Subject>
)
