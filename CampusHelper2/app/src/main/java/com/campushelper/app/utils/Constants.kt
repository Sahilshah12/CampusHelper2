package com.campushelper.app.utils

object Constants {
    // API Base URL - Using local backend temporarily (Render is down)
    // For Android Emulator: use 10.0.2.2 (maps to host machine's localhost)
    // For Physical Device: use your computer's IP address
    const val BASE_URL = "http://10.0.2.2:5002/api/"  // Local backend on port 5002
    // const val BASE_URL = "https://campushelper-be.onrender.com/api/"  // Render backend (currently down)
    
    // SharedPreferences
    const val PREF_NAME = "campus_helper_prefs"
    const val KEY_TOKEN = "jwt_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_NAME = "user_name"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_USER_ROLE = "user_role"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    
    // User Roles
    const val ROLE_STUDENT = "student"
    const val ROLE_ADMIN = "admin"
    
    // API Timeouts (increased to 120s for Render free tier cold starts - can take 60-90s)
    const val CONNECT_TIMEOUT = 120L
    const val READ_TIMEOUT = 120L
    const val WRITE_TIMEOUT = 120L
    
    // Test Configuration
    const val DEFAULT_QUESTION_COUNT = 10
    const val MIN_QUESTION_COUNT = 5
    const val MAX_QUESTION_COUNT = 50
    
    // Difficulty Levels
    const val DIFFICULTY_EASY = "easy"
    const val DIFFICULTY_MEDIUM = "medium"
    const val DIFFICULTY_HARD = "hard"
}
