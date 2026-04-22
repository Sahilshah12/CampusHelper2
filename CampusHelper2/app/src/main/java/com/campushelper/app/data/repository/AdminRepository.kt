package com.campushelper.app.data.repository

import com.campushelper.app.data.model.*
import com.campushelper.app.utils.Constants
import com.campushelper.app.utils.SessionManager
import com.campushelper.app.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer
import java.util.UUID
import javax.inject.Inject

class AdminRepository @Inject constructor(
    private val sessionManager: SessionManager
) {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance()

    private val subjectsRef = firebaseDatabase.reference.child("subjects")
    private val materialsRef = firebaseDatabase.reference.child("materials")

    // Subject Management
    suspend fun createSubject(request: CreateSubjectRequest): Resource<Subject> {
        return withContext(Dispatchers.IO) {
            try {
                val id = subjectsRef.push().key ?: return@withContext Resource.Error("Failed to create subject")
                val subject = Subject(
                    _id = id,
                    name = request.name.trim(),
                    code = request.code.trim().uppercase(),
                    description = request.description,
                    category = request.category,
                    semester = request.semester,
                    credits = request.credits
                )
                subjectsRef.child(id).setValue(subject).await()
                Resource.Success(subject)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun updateSubject(id: String, request: UpdateSubjectRequest): Resource<Subject> {
        return withContext(Dispatchers.IO) {
            try {
                val updated = Subject(
                    _id = id,
                    name = request.name.trim(),
                    code = request.code.trim().uppercase(),
                    description = request.description,
                    category = request.category,
                    semester = request.semester,
                    credits = request.credits
                )
                subjectsRef.child(id).setValue(updated).await()
                Resource.Success(updated)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun deleteSubject(id: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                subjectsRef.child(id).removeValue().await()

                val materials = materialsRef.get().await()
                materials.children.forEach { materialSnap ->
                    val subjectNode = materialSnap.child("subjectId")
                    val materialSubjectId = subjectNode.child("id").getValue(String::class.java)
                        ?: subjectNode.child("_id").getValue(String::class.java)
                    if (materialSubjectId == id) {
                        materialSnap.ref.removeValue().await()
                    }
                }

                Resource.Success("Subject deleted successfully")
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    // Material Management
    suspend fun createMaterial(request: CreateMaterialRequest): Resource<Material> {
        return withContext(Dispatchers.IO) {
            try {
                val subjectSnap = subjectsRef.child(request.subjectId).get().await()
                if (!subjectSnap.exists()) return@withContext Resource.Error("Subject not found")

                val materialId = materialsRef.push().key ?: return@withContext Resource.Error("Failed to create material")
                val normalizedType = normalizeMaterialType(request.type)

                val subjectInfo = MaterialSubjectInfo(
                    id = request.subjectId,
                    name = subjectSnap.child("name").getValue(String::class.java) ?: "Unknown",
                    code = subjectSnap.child("code").getValue(String::class.java)
                )

                val userInfo = MaterialUserInfo(
                    id = sessionManager.getUserId() ?: "admin",
                    name = sessionManager.getUserName() ?: "Admin",
                    email = sessionManager.getUserEmail() ?: "admin@campushelper.com"
                )

                val nowIso = java.time.Instant.now().toString()
                val material = Material(
                    id = materialId,
                    subjectId = subjectInfo,
                    title = request.title,
                    description = request.description,
                    type = normalizedType,
                    url = request.url,
                    fileUrl = if (normalizedType == "pdf") request.url else null,
                    content = null,
                    tags = null,
                    topic = null,
                    difficulty = null,
                    viewCount = 0,
                    uploadedBy = userInfo,
                    isActive = true,
                    createdAt = nowIso,
                    updatedAt = nowIso
                )

                materialsRef.child(materialId).setValue(material).await()
                Resource.Success(material)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun updateMaterial(id: String, request: UpdateMaterialRequest): Resource<Material> {
        return withContext(Dispatchers.IO) {
            try {
                val currentSnap = materialsRef.child(id).get().await()
                if (!currentSnap.exists()) return@withContext Resource.Error("Material not found")

                val current = mapMaterial(currentSnap)
                    ?: return@withContext Resource.Error("Material not found")

                val normalizedType = normalizeMaterialType(request.type)
                val updated = current.copy(
                    title = request.title,
                    type = normalizedType,
                    description = request.description,
                    url = request.url,
                    fileUrl = if (normalizedType == "pdf") (request.url.ifBlank { current.fileUrl }) else null,
                    updatedAt = java.time.Instant.now().toString()
                )

                materialsRef.child(id).setValue(updated).await()
                Resource.Success(updated)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun deleteMaterial(id: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                materialsRef.child(id).removeValue().await()
                Resource.Success("Material deleted successfully")
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    // Competitive Exam Management
    suspend fun createCompetitiveExam(request: CreateCompetitiveExamRequest): Resource<CompetitiveExam> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.Error("Competitive exam backend is not migrated yet")
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun updateCompetitiveExam(id: String, request: UpdateCompetitiveExamRequest): Resource<CompetitiveExam> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.Error("Competitive exam backend is not migrated yet")
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun deleteCompetitiveExam(id: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.Error("Competitive exam backend is not migrated yet")
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    // Upload Material with File
    suspend fun uploadMaterial(
        subjectId: RequestBody,
        title: RequestBody,
        description: RequestBody,
        type: RequestBody,
        file: MultipartBody.Part? = null,
        url: RequestBody? = null
    ): Resource<Material> {
        return withContext(Dispatchers.IO) {
            try {
                val subjectIdValue = requestBodyToString(subjectId)
                val titleValue = requestBodyToString(title)
                val descriptionValue = requestBodyToString(description)
                val typeValue = normalizeMaterialType(requestBodyToString(type))
                val urlValue = url?.let { requestBodyToString(it) }

                if (subjectIdValue.isBlank() || titleValue.isBlank() || typeValue.isBlank()) {
                    return@withContext Resource.Error("Missing required fields")
                }

                val resolvedUrl = if (file != null) {
                    val buffer = Buffer()
                    file.body.writeTo(buffer)
                    val bytes = buffer.readByteArray()
                    if (bytes.isEmpty()) {
                        return@withContext Resource.Error("Selected file is empty")
                    }

                    val fileName = "${UUID.randomUUID()}.pdf"
                    val storageRef = firebaseStorage.reference.child("materials/$fileName")
                    storageRef.putBytes(bytes).await()
                    storageRef.downloadUrl.await().toString()
                } else {
                    urlValue.orEmpty()
                }

                if (resolvedUrl.isBlank() && typeValue != "notes") {
                    return@withContext Resource.Error("Missing material URL or file")
                }

                val subjectSnap = subjectsRef.child(subjectIdValue).get().await()
                if (!subjectSnap.exists()) {
                    return@withContext Resource.Error("Subject not found")
                }

                val materialId = materialsRef.push().key ?: return@withContext Resource.Error("Failed to upload material")
                val subjectInfo = MaterialSubjectInfo(
                    id = subjectIdValue,
                    name = subjectSnap.child("name").getValue(String::class.java) ?: "Unknown",
                    code = subjectSnap.child("code").getValue(String::class.java)
                )
                val userInfo = MaterialUserInfo(
                    id = sessionManager.getUserId() ?: "admin",
                    name = sessionManager.getUserName() ?: "Admin",
                    email = sessionManager.getUserEmail() ?: "admin@campushelper.com"
                )

                val nowIso = java.time.Instant.now().toString()
                val material = Material(
                    id = materialId,
                    subjectId = subjectInfo,
                    title = titleValue,
                    description = descriptionValue,
                    type = typeValue,
                    url = if (typeValue == "youtube" || typeValue == "link") resolvedUrl else null,
                    fileUrl = if (typeValue == "pdf") resolvedUrl else null,
                    content = if (typeValue == "notes") resolvedUrl else null,
                    tags = null,
                    topic = null,
                    difficulty = null,
                    viewCount = 0,
                    uploadedBy = userInfo,
                    isActive = true,
                    createdAt = nowIso,
                    updatedAt = nowIso
                )

                materialsRef.child(materialId).setValue(material).await()
                Resource.Success(material)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred: ${e.localizedMessage}")
            }
        }
    }

    private fun requestBodyToString(body: RequestBody): String {
        val buffer = Buffer()
        body.writeTo(buffer)
        return buffer.readUtf8().trim()
    }

    private fun normalizeMaterialType(rawType: String): String {
        return when (rawType.lowercase()) {
            "video" -> "youtube"
            else -> rawType.lowercase().ifBlank { "link" }
        }
    }

    private fun mapMaterial(materialSnap: com.google.firebase.database.DataSnapshot): Material? {
        val id = materialSnap.child("id").getValue(String::class.java)
            ?: materialSnap.child("_id").getValue(String::class.java)
            ?: materialSnap.key
            ?: return null
        val title = materialSnap.child("title").getValue(String::class.java) ?: return null

        val subjectNode = materialSnap.child("subjectId")
        val subjectInfo = if (subjectNode.exists()) {
            MaterialSubjectInfo(
                id = subjectNode.child("id").getValue(String::class.java)
                    ?: subjectNode.child("_id").getValue(String::class.java)
                    ?: "",
                name = subjectNode.child("name").getValue(String::class.java) ?: "",
                code = subjectNode.child("code").getValue(String::class.java)
            )
        } else null

        val userNode = materialSnap.child("uploadedBy")
        val userInfo = if (userNode.exists()) {
            MaterialUserInfo(
                id = userNode.child("id").getValue(String::class.java)
                    ?: userNode.child("_id").getValue(String::class.java)
                    ?: "",
                name = userNode.child("name").getValue(String::class.java) ?: "",
                email = userNode.child("email").getValue(String::class.java) ?: ""
            )
        } else null

        return Material(
            id = id,
            subjectId = subjectInfo,
            title = title,
            description = materialSnap.child("description").getValue(String::class.java),
            type = materialSnap.child("type").getValue(String::class.java) ?: "link",
            url = materialSnap.child("url").getValue(String::class.java),
            fileUrl = materialSnap.child("fileUrl").getValue(String::class.java),
            content = materialSnap.child("content").getValue(String::class.java),
            tags = null,
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
