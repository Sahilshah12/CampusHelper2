package com.campushelper.app.data.repository

import com.campushelper.app.data.model.Material
import com.campushelper.app.data.model.MaterialSubjectInfo
import com.campushelper.app.data.model.MaterialUserInfo
import com.campushelper.app.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MaterialRepository @Inject constructor() {

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private val materialsRef = firebaseDatabase.reference.child("materials")

    suspend fun getMaterials(): Resource<List<Material>> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = materialsRef.get().await()
                val materials = snapshot.children.mapNotNull { mapMaterial(it) }
                    .filter { it.isActive }
                Resource.Success(materials)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getMaterialsBySubject(subjectId: String): Resource<List<Material>> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = materialsRef.get().await()
                val materials = snapshot.children.mapNotNull { mapMaterial(it) }
                    .filter { it.isActive && it.subjectId?.id == subjectId }
                Resource.Success(materials)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    private fun mapMaterial(materialSnap: com.google.firebase.database.DataSnapshot): Material? {
        val id = materialSnap.child("id").getValue(String::class.java)
            ?: materialSnap.child("_id").getValue(String::class.java)
            ?: materialSnap.key
            ?: return null
        val title = materialSnap.child("title").getValue(String::class.java) ?: return null
        val typeRaw = materialSnap.child("type").getValue(String::class.java) ?: "link"
        val type = if (typeRaw.equals("video", ignoreCase = true)) "youtube" else typeRaw

        val subjectIdValue = materialSnap.child("subjectId")
        val subjectInfo = if (subjectIdValue.exists()) {
            MaterialSubjectInfo(
                id = subjectIdValue.child("id").getValue(String::class.java)
                    ?: subjectIdValue.child("_id").getValue(String::class.java)
                    ?: "",
                name = subjectIdValue.child("name").getValue(String::class.java) ?: "",
                code = subjectIdValue.child("code").getValue(String::class.java)
            )
        } else {
            null
        }

        val uploadedByValue = materialSnap.child("uploadedBy")
        val userInfo = if (uploadedByValue.exists()) {
            MaterialUserInfo(
                id = uploadedByValue.child("id").getValue(String::class.java)
                    ?: uploadedByValue.child("_id").getValue(String::class.java)
                    ?: "",
                name = uploadedByValue.child("name").getValue(String::class.java) ?: "",
                email = uploadedByValue.child("email").getValue(String::class.java) ?: ""
            )
        } else {
            null
        }

        val tags = materialSnap.child("tags").children.mapNotNull { it.getValue(String::class.java) }

        return Material(
            id = id,
            subjectId = subjectInfo,
            title = title,
            description = materialSnap.child("description").getValue(String::class.java),
            type = type,
            url = materialSnap.child("url").getValue(String::class.java),
            fileUrl = materialSnap.child("fileUrl").getValue(String::class.java),
            content = materialSnap.child("content").getValue(String::class.java),
            tags = if (tags.isEmpty()) null else tags,
            topic = materialSnap.child("topic").getValue(String::class.java),
            difficulty = materialSnap.child("difficulty").getValue(String::class.java),
            viewCount = materialSnap.child("viewCount").getValue(Int::class.java) ?: 0,
            uploadedBy = userInfo,
            isActive = materialSnap.child("isActive").getValue(Boolean::class.java) ?: true,
            createdAt = materialSnap.child("createdAt").getValue(String::class.java),
            updatedAt = materialSnap.child("updatedAt").getValue(String::class.java)
        )
    }
}
