package com.campushelper.app.data.repository

import com.campushelper.app.data.model.AuthResponse
import com.campushelper.app.data.model.LoginRequest
import com.campushelper.app.data.model.RegisterRequest
import com.campushelper.app.data.remote.ApiService
import com.campushelper.app.utils.Resource
import com.campushelper.app.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun register(request: RegisterRequest): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(request)
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    saveUserSession(authResponse)
                    Resource.Success(authResponse)
                } else {
                    Resource.Error(parseApiError(response.errorBody()?.string(), "Registration failed"))
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun login(request: LoginRequest): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(request)
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    saveUserSession(authResponse)
                    Resource.Success(authResponse)
                } else {
                    Resource.Error(parseApiError(response.errorBody()?.string(), "Login failed"))
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    private fun saveUserSession(authResponse: AuthResponse) {
        sessionManager.saveToken(authResponse.token)
        sessionManager.saveUserData(
            authResponse.user.id,
            authResponse.user.name,
            authResponse.user.email,
            authResponse.user.role
        )
    }

    private fun parseApiError(rawErrorBody: String?, fallback: String): String {
        if (rawErrorBody.isNullOrBlank()) return fallback

        return try {
            val json = JSONObject(rawErrorBody)
            when {
                json.has("error") -> json.optString("error", fallback)
                json.has("message") -> json.optString("message", fallback)
                else -> fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    fun getUserRole(): String? {
        return sessionManager.getUserRole()
    }
}
