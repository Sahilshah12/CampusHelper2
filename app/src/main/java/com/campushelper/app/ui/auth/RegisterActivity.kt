package com.campushelper.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.campushelper.app.databinding.ActivityRegisterBinding
import com.campushelper.app.ui.student.StudentDashboardActivity
import com.campushelper.app.ui.viewmodel.AuthViewModel
import com.campushelper.app.utils.Constants
import com.campushelper.app.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etName.requestFocus()
        
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.etName.addTextChangedListener { binding.tilName.error = null }
        binding.etEmail.addTextChangedListener { binding.tilEmail.error = null }
        binding.etPassword.addTextChangedListener { binding.tilPassword.error = null }

        binding.btnRegister.setOnClickListener {
            validateAndRegister()
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateAndRegister() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        // Always register as student - admin registration disabled
        val role = Constants.ROLE_STUDENT

        var isValid = true

        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        }

        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (isValid) {
            viewModel.register(name, email, password, role)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.registerState.collect { resource ->
                when (resource) {
                    null -> {
                        // Initial state - do nothing
                        binding.btnRegister.isEnabled = true
                        binding.btnRegister.text = "Register"
                    }
                    is Resource.Loading -> {
                        binding.btnRegister.isEnabled = false
                        binding.btnRegister.text = "Registering..."
                    }
                    is Resource.Success -> {
                        binding.btnRegister.isEnabled = true
                        binding.btnRegister.text = "Register"
                        
                        // Always navigate to Student Dashboard (admin registration disabled)
                        val intent = Intent(this@RegisterActivity, StudentDashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    is Resource.Error -> {
                        binding.btnRegister.isEnabled = true
                        binding.btnRegister.text = "Register"
                        Toast.makeText(this@RegisterActivity, resource.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
