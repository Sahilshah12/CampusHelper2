package com.campushelper.app.data.repository

import com.campushelper.app.data.model.AuthResponse
import com.campushelper.app.data.model.LoginRequest
import com.campushelper.app.data.model.RegisterRequest
import com.campushelper.app.data.model.User
import com.campushelper.app.utils.Resource
import com.campushelper.app.utils.Constants
import com.campushelper.app.utils.SessionManager
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val sessionManager: SessionManager
) {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val usersRef = FirebaseDatabase.getInstance().reference.child("users")

    suspend fun register(request: RegisterRequest): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val result = firebaseAuth
                    .createUserWithEmailAndPassword(request.email.trim(), request.password)
                    .await()

                val firebaseUser = result.user
                    ?: return@withContext Resource.Error("Registration failed")

                val safeRole = when (request.role.lowercase()) {
                    Constants.ROLE_ADMIN -> Constants.ROLE_ADMIN
                    else -> Constants.ROLE_STUDENT
                }

                val user = User(
                    id = firebaseUser.uid,
                    name = request.name.trim(),
                    email = request.email.trim(),
                    role = safeRole
                )

                // Keep profile in Firebase Auth and app-specific fields in Realtime Database.
                firebaseUser.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(user.name)
                        .build()
                ).await()

                usersRef.child(firebaseUser.uid).setValue(user).await()

                val token = firebaseUser.getIdToken(false).await().token.orEmpty()
                val authResponse = AuthResponse(success = true, token = token, user = user)
                saveUserSession(authResponse)
                Resource.Success(authResponse)
            } catch (e: Exception) {
                Resource.Error(parseFirebaseError(e, "Registration failed"))
            }
        }
    }

    suspend fun login(request: LoginRequest): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val result = firebaseAuth
                    .signInWithEmailAndPassword(request.email.trim(), request.password)
                    .await()

                val firebaseUser = result.user
                    ?: return@withContext Resource.Error("Login failed")

                val snapshot = usersRef.child(firebaseUser.uid).get().await()
                val user = if (snapshot.exists()) {
                    User(
                        id = firebaseUser.uid,
                        name = snapshot.child("name").getValue(String::class.java)
                            ?: firebaseUser.displayName
                            ?: request.email.substringBefore("@"),
                        email = snapshot.child("email").getValue(String::class.java)
                            ?: firebaseUser.email
                            ?: request.email.trim(),
                        role = snapshot.child("role").getValue(String::class.java)
                            ?: Constants.ROLE_STUDENT
                    )
                } else {
                    val createdUser = User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: request.email.substringBefore("@"),
                        email = firebaseUser.email ?: request.email.trim(),
                        role = Constants.ROLE_STUDENT
                    )
                    usersRef.child(firebaseUser.uid).setValue(createdUser).await()
                    createdUser
                }

                val token = firebaseUser.getIdToken(false).await().token.orEmpty()
                val authResponse = AuthResponse(success = true, token = token, user = user)
                saveUserSession(authResponse)
                Resource.Success(authResponse)
            } catch (e: Exception) {
                Resource.Error(parseFirebaseError(e, "Login failed"))
            }
        }
    }

    suspend fun updateProfile(name: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = firebaseAuth.currentUser
                    ?: return@withContext Resource.Error("User not logged in")

                val newName = name.trim()
                currentUser.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build()
                ).await()

                usersRef.child(currentUser.uid).child("name").setValue(newName).await()

                sessionManager.saveUserData(
                    currentUser.uid,
                    newName,
                    currentUser.email.orEmpty(),
                    sessionManager.getUserRole() ?: Constants.ROLE_STUDENT
                )

                Resource.Success("Profile updated successfully")
            } catch (e: Exception) {
                Resource.Error(parseFirebaseError(e, "Failed to update profile"))
            }
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = firebaseAuth.currentUser
                    ?: return@withContext Resource.Error("User not logged in")
                val email = currentUser.email
                    ?: return@withContext Resource.Error("User email not available")

                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                currentUser.reauthenticate(credential).await()
                currentUser.updatePassword(newPassword).await()

                Resource.Success("Password changed successfully")
            } catch (e: Exception) {
                Resource.Error(parseFirebaseError(e, "Failed to change password"))
            }
        }
    }

    suspend fun deleteAccount(currentPassword: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = firebaseAuth.currentUser
                    ?: return@withContext Resource.Error("User not logged in")
                val email = currentUser.email
                    ?: return@withContext Resource.Error("User email not available")

                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                currentUser.reauthenticate(credential).await()

                usersRef.child(currentUser.uid).removeValue().await()
                currentUser.delete().await()

                sessionManager.clearSession()
                Resource.Success("Account deleted successfully")
            } catch (e: Exception) {
                Resource.Error(parseFirebaseError(e, "Failed to delete account"))
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

    private fun parseFirebaseError(error: Exception, fallback: String): String {
        return when (error) {
            is FirebaseAuthException -> when (error.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Invalid email address"
                "ERROR_USER_NOT_FOUND" -> "No account found for this email"
                "ERROR_WRONG_PASSWORD" -> "Incorrect password"
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Email is already in use"
                "ERROR_WEAK_PASSWORD" -> "Password is too weak"
                "ERROR_INVALID_CREDENTIAL" -> "Invalid credentials"
                "ERROR_REQUIRES_RECENT_LOGIN" -> "Please login again and retry"
                else -> error.localizedMessage ?: fallback
            }
            else -> error.localizedMessage ?: fallback
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        sessionManager.clearSession()
    }

    fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null || sessionManager.isLoggedIn()
    }

    fun getUserRole(): String? {
        return sessionManager.getUserRole()
    }
}
