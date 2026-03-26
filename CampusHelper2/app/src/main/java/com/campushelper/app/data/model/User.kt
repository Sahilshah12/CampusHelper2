package com.campushelper.app.data.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "student"
)

data class AuthResponse(
    val success: Boolean,
    val token: String,
    val user: User
)

data class UsersResponse(
    val success: Boolean,
    val count: Int,
    val users: List<User>
)
