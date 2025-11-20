package com.campushelper.app.data.remote

import com.campushelper.app.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Authentication
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<AuthResponse>
    
    @GET("auth/users")
    suspend fun getAllUsers(): Response<UsersResponse>
    
    // Subjects
    @GET("subjects")
    suspend fun getSubjects(
        @Query("category") category: String? = null,
        @Query("semester") semester: Int? = null
    ): Response<SubjectsResponse>
    
    @GET("subjects/{id}")
    suspend fun getSubject(@Path("id") id: String): Response<Subject>
    
    // Admin - Subject Management
    @POST("subjects")
    suspend fun createSubject(@Body subject: CreateSubjectRequest): Response<SubjectResponse>
    
    @PUT("subjects/{id}")
    suspend fun updateSubject(
        @Path("id") id: String,
        @Body subject: UpdateSubjectRequest
    ): Response<SubjectResponse>
    
    @DELETE("subjects/{id}")
    suspend fun deleteSubject(@Path("id") id: String): Response<DeleteResponse>
    
    // Materials
    @GET("materials")
    suspend fun getMaterials(
        @Query("subjectId") subjectId: String
    ): Response<MaterialsResponse>
    
    // Admin - Material Management
    @POST("materials")
    suspend fun createMaterial(@Body material: CreateMaterialRequest): Response<MaterialResponse>
    
    @PUT("materials/{id}")
    suspend fun updateMaterial(
        @Path("id") id: String,
        @Body material: UpdateMaterialRequest
    ): Response<MaterialResponse>
    
    @DELETE("materials/{id}")
    suspend fun deleteMaterial(@Path("id") id: String): Response<DeleteResponse>
    
    // Upload Material with File
    @Multipart
    @POST("materials")
    suspend fun uploadMaterial(
        @Part("subjectId") subjectId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("type") type: RequestBody,
        @Part file: MultipartBody.Part? = null,
        @Part("url") url: RequestBody? = null
    ): Response<MaterialResponse>
    
    // AI Features
    @POST("ai/chat")
    suspend fun aiChat(@Body request: AiChatRequest): Response<AiChatResponse>
    
    // Tests
    @POST("tests/generate")
    suspend fun generateTest(@Body request: TestRequest): Response<TestResponse>
    
    @GET("tests")
    suspend fun getTests(
        @Query("status") status: String? = null,
        @Query("subjectId") subjectId: String? = null
    ): Response<TestsResponse>
    
    @GET("tests/{id}")
    suspend fun getTest(@Path("id") id: String): Response<TestResponse>
    
    @POST("tests/{id}/submit")
    suspend fun submitTest(
        @Path("id") id: String,
        @Body request: SubmitTestRequest
    ): Response<TestResultsResponse>
    
    @GET("tests/{id}/analysis")
    suspend fun getTestAnalysis(@Path("id") id: String): Response<TestAnalysisResponse>
    
    // Progress
    @GET("progress")
    suspend fun getProgress(): Response<ProgressResponse>
    
    // Competitive Exams
    @GET("competitive-exams")
    suspend fun getCompetitiveExams(): Response<CompetitiveExamsResponse>
    
    @GET("competitive-exams/{id}")
    suspend fun getCompetitiveExam(@Path("id") id: String): Response<CompetitiveExam>
    
    @POST("competitive-exams/{id}/enroll")
    suspend fun enrollInExam(
        @Path("id") id: String,
        @Body request: EnrollRequest
    ): Response<EnrollResponse>
    
    @POST("competitive-exams/{id}/generate-test")
    suspend fun generateCompetitiveTest(@Path("id") id: String): Response<TestResponse>
    
    // Admin - Competitive Exam Management
    @POST("competitive-exams")
    suspend fun createCompetitiveExam(@Body exam: CreateCompetitiveExamRequest): Response<CompetitiveExamResponse>
    
    @PUT("competitive-exams/{id}")
    suspend fun updateCompetitiveExam(
        @Path("id") id: String,
        @Body exam: UpdateCompetitiveExamRequest
    ): Response<CompetitiveExamResponse>
    
    @DELETE("competitive-exams/{id}")
    suspend fun deleteCompetitiveExam(@Path("id") id: String): Response<DeleteResponse>
}
