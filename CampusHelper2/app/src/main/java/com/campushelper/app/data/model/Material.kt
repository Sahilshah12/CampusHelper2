package com.campushelper.app.data.model

import com.google.gson.annotations.SerializedName

data class Material(
    @SerializedName("_id")
    val id: String,
    val subjectId: MaterialSubjectInfo? = null,
    val title: String,
    val description: String? = null,
    val type: String, // "pdf", "youtube", "link", "notes"
    val url: String? = null,
    val fileUrl: String? = null,
    val content: String? = null,
    val tags: List<String>? = null,
    val topic: String? = null,
    val difficulty: String? = null,
    val viewCount: Int? = 0,
    val uploadedBy: MaterialUserInfo? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class MaterialSubjectInfo(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val code: String? = null
)

data class MaterialUserInfo(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val email: String
)

data class MaterialsResponse(
    val success: Boolean,
    val count: Int? = null,
    val materials: List<Material>
)
