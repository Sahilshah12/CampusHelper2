package com.campushelper.app.data.model

import com.google.gson.annotations.SerializedName

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

data class UpdateProfileRequest(
    val name: String,
    val profileImage: String? = null
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class DeleteAccountRequest(
    val currentPassword: String
)

data class ProfileResponse(
    val success: Boolean,
    val user: ProfileUser
)

data class ProfileUser(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val profileImage: String? = null
)

data class MessageResponse(
    val success: Boolean,
    val message: String
)

data class UsersResponse(
    val success: Boolean,
    val count: Int,
    val users: List<User>
)
