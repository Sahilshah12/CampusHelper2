package com.campushelper.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.campushelper.app.R
import com.campushelper.app.databinding.ActivityLoginBinding
import com.campushelper.app.ui.admin.AdminDashboardActivity
import com.campushelper.app.ui.student.StudentDashboardActivity
import com.campushelper.app.ui.viewmodel.AuthViewModel
import com.campushelper.app.utils.Constants
import com.campushelper.app.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request focus on email field to show keyboard
        binding.etEmail.requestFocus()
        
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.etEmail.addTextChangedListener {
            binding.tilEmail.error = null
        }

        binding.etPassword.addTextChangedListener {
            binding.tilPassword.error = null
        }

        binding.btnLogin.setOnClickListener {
            validateAndLogin()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateAndLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        }

        if (isValid) {
            viewModel.login(email, password)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { resource ->
                    when (resource) {
                        null -> {
                            // Initial state - do nothing
                            binding.btnLogin.isEnabled = true
                            binding.btnLogin.text = "Login"
                        }
                        is Resource.Loading -> {
                            binding.btnLogin.isEnabled = false
                            binding.btnLogin.text = "Logging in..."
                        }
                        is Resource.Success -> {
                            binding.btnLogin.isEnabled = true
                            binding.btnLogin.text = "Login"
                            
                            val role = resource.data?.user?.role
                            val intent = when (role) {
                                Constants.ROLE_ADMIN -> Intent(this@LoginActivity, AdminDashboardActivity::class.java)
                                else -> Intent(this@LoginActivity, StudentDashboardActivity::class.java)
                            }
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        is Resource.Error -> {
                            binding.btnLogin.isEnabled = true
                            binding.btnLogin.text = "Login"
                            Toast.makeText(this@LoginActivity, resource.message ?: "Login failed", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}
