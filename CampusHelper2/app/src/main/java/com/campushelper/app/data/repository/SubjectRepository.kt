package com.campushelper.app.data.repository

import com.campushelper.app.data.model.Material
import com.campushelper.app.data.model.MaterialSubjectInfo
import com.campushelper.app.data.model.MaterialUserInfo
import com.campushelper.app.data.model.Subject
import com.campushelper.app.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SubjectRepository @Inject constructor() {

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private val subjectsRef = firebaseDatabase.reference.child("subjects")
    private val materialsRef = firebaseDatabase.reference.child("materials")

    suspend fun getSubjects(): Resource<List<Subject>> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = subjectsRef.get().await()
                val subjects = snapshot.children.mapNotNull { subjectSnap ->
                    val id = subjectSnap.child("_id").getValue(String::class.java)
                        ?: subjectSnap.key
                        ?: return@mapNotNull null
                    val name = subjectSnap.child("name").getValue(String::class.java) ?: return@mapNotNull null
                    val code = subjectSnap.child("code").getValue(String::class.java) ?: return@mapNotNull null
                    val description = subjectSnap.child("description").getValue(String::class.java)
                    val category = subjectSnap.child("category").getValue(String::class.java) ?: "Engineering"
                    val semester = subjectSnap.child("semester").getValue(Int::class.java) ?: 1
                    val credits = subjectSnap.child("credits").getValue(Int::class.java) ?: 3

                    Subject(
                        _id = id,
                        name = name,
                        code = code,
                        description = description,
                        category = category,
                        semester = semester,
                        credits = credits
                    )
                }.sortedBy { it.name.lowercase() }

                Resource.Success(subjects)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getSubject(id: String): Resource<Subject> {
        return withContext(Dispatchers.IO) {
            try {
                val subjectSnap = subjectsRef.child(id).get().await()
                if (!subjectSnap.exists()) {
                    return@withContext Resource.Error("Subject not found")
                }

                val subject = Subject(
                    _id = subjectSnap.child("_id").getValue(String::class.java) ?: id,
                    name = subjectSnap.child("name").getValue(String::class.java)
                        ?: return@withContext Resource.Error("Invalid subject data"),
                    code = subjectSnap.child("code").getValue(String::class.java)
                        ?: return@withContext Resource.Error("Invalid subject data"),
                    description = subjectSnap.child("description").getValue(String::class.java),
                    category = subjectSnap.child("category").getValue(String::class.java) ?: "Engineering",
                    semester = subjectSnap.child("semester").getValue(Int::class.java) ?: 1,
                    credits = subjectSnap.child("credits").getValue(Int::class.java) ?: 3
                )

                Resource.Success(subject)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getMaterials(subjectId: String): Resource<List<Material>> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = materialsRef.get().await()
                val materials = snapshot.children.mapNotNull { materialSnap ->
                    val material = mapMaterial(materialSnap)
                    if (material != null && material.subjectId?.id == subjectId && material.isActive) {
                        material
                    } else {
                        null
                    }
                }

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
