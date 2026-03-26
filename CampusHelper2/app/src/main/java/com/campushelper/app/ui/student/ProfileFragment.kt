package com.campushelper.app.ui.student

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.campushelper.app.databinding.FragmentProfileBinding
import com.campushelper.app.ui.auth.LoginActivity
import com.campushelper.app.ui.viewmodel.AuthViewModel
import com.campushelper.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
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
            
            // Navigate to login screen
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
        
        binding.cardEditProfile.setOnClickListener {
            // TODO: Navigate to edit profile screen
        }
        
        binding.cardChangePassword.setOnClickListener {
            // TODO: Navigate to change password screen
        }
        
        binding.cardSettings.setOnClickListener {
            // TODO: Navigate to settings screen
        }
        
        binding.cardAbout.setOnClickListener {
            // TODO: Show about dialog
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
