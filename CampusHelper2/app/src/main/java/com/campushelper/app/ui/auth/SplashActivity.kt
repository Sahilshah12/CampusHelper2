package com.campushelper.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.campushelper.app.R
import com.campushelper.app.ui.admin.AdminDashboardActivity
import com.campushelper.app.ui.student.StudentDashboardActivity
import com.campushelper.app.ui.viewmodel.AuthViewModel
import com.campushelper.app.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SplashActivity", "onCreate called")
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("SplashActivity", "Handler executing after delay")
            checkLoginStatus()
        }, 2000)
    }

    private fun checkLoginStatus() {
        try {
            Log.d("SplashActivity", "Checking login status...")
            val isLoggedIn = viewModel.isLoggedIn()
            Log.d("SplashActivity", "Is logged in: $isLoggedIn")
            
            if (isLoggedIn) {
                val role = viewModel.getUserRole()
                Log.d("SplashActivity", "User role: $role")
                val intent = when (role) {
                    Constants.ROLE_ADMIN -> Intent(this, AdminDashboardActivity::class.java)
                    else -> Intent(this, StudentDashboardActivity::class.java)
                }
                startActivity(intent)
            } else {
                Log.d("SplashActivity", "User not logged in, navigating to LoginActivity")
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error in checkLoginStatus: ${e.message}", e)
            // Fallback to LoginActivity on error
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
