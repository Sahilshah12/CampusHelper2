package com.campushelper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campushelper.app.data.model.AuthResponse
import com.campushelper.app.data.model.LoginRequest
import com.campushelper.app.data.model.RegisterRequest
import com.campushelper.app.data.repository.AuthRepository
import com.campushelper.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val loginState: StateFlow<Resource<AuthResponse>?> = _loginState

    private val _registerState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val registerState: StateFlow<Resource<AuthResponse>?> = _registerState

    private val _updateProfileState = MutableStateFlow<Resource<String>?>(null)
    val updateProfileState: StateFlow<Resource<String>?> = _updateProfileState

    private val _changePasswordState = MutableStateFlow<Resource<String>?>(null)
    val changePasswordState: StateFlow<Resource<String>?> = _changePasswordState

    private val _deleteAccountState = MutableStateFlow<Resource<String>?>(null)
    val deleteAccountState: StateFlow<Resource<String>?> = _deleteAccountState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = authRepository.login(LoginRequest(email, password))
            _loginState.value = result
        }
    }

    fun register(name: String, email: String, password: String, role: String = "student") {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            val result = authRepository.register(RegisterRequest(name, email, password, role))
            _registerState.value = result
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun updateProfile(name: String) {
        viewModelScope.launch {
            _updateProfileState.value = Resource.Loading()
            _updateProfileState.value = authRepository.updateProfile(name)
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = Resource.Loading()
            _changePasswordState.value = authRepository.changePassword(currentPassword, newPassword)
        }
    }

    fun deleteAccount(currentPassword: String) {
        viewModelScope.launch {
            _deleteAccountState.value = Resource.Loading()
            _deleteAccountState.value = authRepository.deleteAccount(currentPassword)
        }
    }

    fun resetUpdateProfileState() {
        _updateProfileState.value = null
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = null
    }

    fun resetDeleteAccountState() {
        _deleteAccountState.value = null
    }

    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }

    fun getUserRole(): String? {
        return authRepository.getUserRole()
    }
}
