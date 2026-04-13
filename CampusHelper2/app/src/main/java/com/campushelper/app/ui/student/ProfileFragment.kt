package com.campushelper.app.ui.student

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.campushelper.app.databinding.FragmentProfileBinding
import com.campushelper.app.ui.auth.LoginActivity
import com.campushelper.app.ui.viewmodel.AuthViewModel
import com.campushelper.app.utils.Resource
import com.campushelper.app.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()
    
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUserInfo()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUserInfo() {
        val userName = sessionManager.getUserName() ?: "User"
        val userEmail = sessionManager.getUserEmail() ?: ""
        val userRole = sessionManager.getUserRole() ?: "Student"
        
        binding.tvUserName.text = userName
        binding.tvUserEmail.text = userEmail
        binding.tvUserRole.text = userRole.replaceFirstChar { it.uppercase() }
        
        // Set initial letter for avatar
        binding.tvUserInitial.text = userName.firstOrNull()?.uppercase() ?: "U"
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            navigateToLogin()
        }
        
        binding.cardEditProfile.setOnClickListener {
            showEditProfileDialog()
        }
        
        binding.cardChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
        
        binding.cardSettings.setOnClickListener {
            showSettingsDialog()
        }
        
        binding.cardAbout.setOnClickListener {
            showAboutDialog()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateProfileState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> setActionsEnabled(false)
                    is Resource.Success -> {
                        setActionsEnabled(true)
                        Toast.makeText(requireContext(), resource.data ?: "Profile updated", Toast.LENGTH_SHORT).show()
                        setupUserInfo()
                        viewModel.resetUpdateProfileState()
                    }
                    is Resource.Error -> {
                        setActionsEnabled(true)
                        Toast.makeText(requireContext(), resource.message ?: "Failed to update profile", Toast.LENGTH_SHORT).show()
                        viewModel.resetUpdateProfileState()
                    }
                    null -> setActionsEnabled(true)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.changePasswordState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> setActionsEnabled(false)
                    is Resource.Success -> {
                        setActionsEnabled(true)
                        Toast.makeText(requireContext(), resource.data ?: "Password changed", Toast.LENGTH_SHORT).show()
                        viewModel.resetChangePasswordState()
                    }
                    is Resource.Error -> {
                        setActionsEnabled(true)
                        Toast.makeText(requireContext(), resource.message ?: "Failed to change password", Toast.LENGTH_SHORT).show()
                        viewModel.resetChangePasswordState()
                    }
                    null -> setActionsEnabled(true)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteAccountState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> setActionsEnabled(false)
                    is Resource.Success -> {
                        setActionsEnabled(true)
                        Toast.makeText(requireContext(), resource.data ?: "Account deleted", Toast.LENGTH_SHORT).show()
                        viewModel.resetDeleteAccountState()
                        navigateToLogin()
                    }
                    is Resource.Error -> {
                        setActionsEnabled(true)
                        Toast.makeText(requireContext(), resource.message ?: "Failed to delete account", Toast.LENGTH_SHORT).show()
                        viewModel.resetDeleteAccountState()
                    }
                    null -> setActionsEnabled(true)
                }
            }
        }
    }

    private fun showEditProfileDialog() {
        val input = EditText(requireContext()).apply {
            setText(sessionManager.getUserName() ?: "")
            hint = "Enter your name"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            setPadding(40, 30, 40, 30)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Profile")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.length < 2) {
                    Toast.makeText(requireContext(), "Name must be at least 2 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.updateProfile(newName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val currentPasswordInput = EditText(requireContext()).apply {
            hint = "Current Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val newPasswordInput = EditText(requireContext()).apply {
            hint = "New Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val confirmPasswordInput = EditText(requireContext()).apply {
            hint = "Confirm New Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        container.addView(currentPasswordInput)
        container.addView(newPasswordInput)
        container.addView(confirmPasswordInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Password")
            .setView(container)
            .setPositiveButton("Update") { _, _ ->
                val currentPassword = currentPasswordInput.text.toString().trim()
                val newPassword = newPasswordInput.text.toString().trim()
                val confirmPassword = confirmPasswordInput.text.toString().trim()

                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword.length < 6) {
                    Toast.makeText(requireContext(), "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(requireContext(), "New passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewModel.changePassword(currentPassword, newPassword)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteAccountDialog() {
        val passwordInput = EditText(requireContext()).apply {
            hint = "Enter current password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(40, 30, 40, 30)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Account")
            .setMessage("This action is permanent and cannot be undone.")
            .setView(passwordInput)
            .setPositiveButton("Delete") { _, _ ->
                val password = passwordInput.text.toString().trim()
                if (password.isEmpty()) {
                    Toast.makeText(requireContext(), "Password is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.deleteAccount(password)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSettingsDialog() {
        val prefs = requireContext().getSharedPreferences("campus_helper_settings", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)

        val options = arrayOf("Notifications ON", "Notifications OFF")
        val checkedItem = if (notificationsEnabled) 0 else 1

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Settings")
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                val enabled = which == 0
                prefs.edit().putBoolean("notifications_enabled", enabled).apply()
                Toast.makeText(requireContext(), if (enabled) "Notifications enabled" else "Notifications disabled", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showAboutDialog() {
        val appVersion = try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            packageInfo.versionName
        } catch (_: Exception) {
            "1.0"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("About Campus Helper")
            .setMessage(
                "Campus Helper\n" +
                    "Version: $appVersion\n\n" +
                    "An AI-powered educational assistant for students.\n\n" +
                    "Privacy Policy:\nhttps://campushelper-bes.onrender.com/privacy-policy"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setActionsEnabled(enabled: Boolean) {
        binding.btnLogout.isEnabled = enabled
        binding.btnDeleteAccount.isEnabled = enabled
        binding.cardEditProfile.isEnabled = enabled
        binding.cardChangePassword.isEnabled = enabled
        binding.cardSettings.isEnabled = enabled
        binding.cardAbout.isEnabled = enabled
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
