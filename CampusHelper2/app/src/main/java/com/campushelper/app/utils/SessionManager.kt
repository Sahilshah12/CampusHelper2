package com.campushelper.app.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREF_NAME,
        Context.MODE_PRIVATE
    )

    fun saveToken(token: String) {
        prefs.edit().putString(Constants.KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(Constants.KEY_TOKEN, null)
    }

    fun saveUserData(userId: String, name: String, email: String, role: String) {
        prefs.edit().apply {
            putString(Constants.KEY_USER_ID, userId)
            putString(Constants.KEY_USER_NAME, name)
            putString(Constants.KEY_USER_EMAIL, email)
            putString(Constants.KEY_USER_ROLE, role)
            putBoolean(Constants.KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getUserId(): String? = prefs.getString(Constants.KEY_USER_ID, null)
    fun getUserName(): String? = prefs.getString(Constants.KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(Constants.KEY_USER_EMAIL, null)
    fun getUserRole(): String? = prefs.getString(Constants.KEY_USER_ROLE, null)
    fun isLoggedIn(): Boolean = prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false)

    fun clearSession() {
        prefs.edit().clear().commit() // Use commit() for immediate synchronous write
    }
}
