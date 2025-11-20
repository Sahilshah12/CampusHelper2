package com.campushelper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campushelper.app.data.model.*
import com.campushelper.app.data.repository.AdminRepository
import com.campushelper.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    // Subject Management
    private val _createSubjectState = MutableStateFlow<Resource<Subject>?>(null)
    val createSubjectState: StateFlow<Resource<Subject>?> = _createSubjectState

    private val _updateSubjectState = MutableStateFlow<Resource<Subject>?>(null)
    val updateSubjectState: StateFlow<Resource<Subject>?> = _updateSubjectState

    private val _deleteSubjectState = MutableStateFlow<Resource<String>?>(null)
    val deleteSubjectState: StateFlow<Resource<String>?> = _deleteSubjectState

    // Material Management
    private val _createMaterialState = MutableStateFlow<Resource<Material>?>(null)
    val createMaterialState: StateFlow<Resource<Material>?> = _createMaterialState

    private val _updateMaterialState = MutableStateFlow<Resource<Material>?>(null)
    val updateMaterialState: StateFlow<Resource<Material>?> = _updateMaterialState

    private val _deleteMaterialState = MutableStateFlow<Resource<String>?>(null)
    val deleteMaterialState: StateFlow<Resource<String>?> = _deleteMaterialState

    // Competitive Exam Management
    private val _createExamState = MutableStateFlow<Resource<CompetitiveExam>?>(null)
    val createExamState: StateFlow<Resource<CompetitiveExam>?> = _createExamState

    private val _updateExamState = MutableStateFlow<Resource<CompetitiveExam>?>(null)
    val updateExamState: StateFlow<Resource<CompetitiveExam>?> = _updateExamState

    private val _deleteExamState = MutableStateFlow<Resource<String>?>(null)
    val deleteExamState: StateFlow<Resource<String>?> = _deleteExamState

    // Upload Material with File
    private val _uploadMaterialState = MutableStateFlow<Resource<Material>?>(null)
    val uploadMaterialState: StateFlow<Resource<Material>?> = _uploadMaterialState

    // Subject Management Functions
    fun createSubject(request: CreateSubjectRequest) {
        viewModelScope.launch {
            _createSubjectState.value = Resource.Loading()
            val result = adminRepository.createSubject(request)
            _createSubjectState.value = result
        }
    }

    fun updateSubject(id: String, request: UpdateSubjectRequest) {
        viewModelScope.launch {
            _updateSubjectState.value = Resource.Loading()
            val result = adminRepository.updateSubject(id, request)
            _updateSubjectState.value = result
        }
    }

    fun deleteSubject(id: String) {
        viewModelScope.launch {
            _deleteSubjectState.value = Resource.Loading()
            val result = adminRepository.deleteSubject(id)
            _deleteSubjectState.value = result
        }
    }

    // Material Management Functions
    fun createMaterial(request: CreateMaterialRequest) {
        viewModelScope.launch {
            _createMaterialState.value = Resource.Loading()
            val result = adminRepository.createMaterial(request)
            _createMaterialState.value = result
        }
    }

    fun updateMaterial(id: String, request: UpdateMaterialRequest) {
        viewModelScope.launch {
            _updateMaterialState.value = Resource.Loading()
            val result = adminRepository.updateMaterial(id, request)
            _updateMaterialState.value = result
        }
    }

    fun deleteMaterial(id: String) {
        viewModelScope.launch {
            _deleteMaterialState.value = Resource.Loading()
            val result = adminRepository.deleteMaterial(id)
            _deleteMaterialState.value = result
        }
    }

    // Competitive Exam Management Functions
    fun createCompetitiveExam(request: CreateCompetitiveExamRequest) {
        viewModelScope.launch {
            _createExamState.value = Resource.Loading()
            val result = adminRepository.createCompetitiveExam(request)
            _createExamState.value = result
        }
    }

    fun updateCompetitiveExam(id: String, request: UpdateCompetitiveExamRequest) {
        viewModelScope.launch {
            _updateExamState.value = Resource.Loading()
            val result = adminRepository.updateCompetitiveExam(id, request)
            _updateExamState.value = result
        }
    }

    fun deleteCompetitiveExam(id: String) {
        viewModelScope.launch {
            _deleteExamState.value = Resource.Loading()
            val result = adminRepository.deleteCompetitiveExam(id)
            _deleteExamState.value = result
        }
    }

    // Reset states
    fun resetCreateSubjectState() {
        _createSubjectState.value = null
    }

    fun resetDeleteSubjectState() {
        _deleteSubjectState.value = null
    }

    fun resetCreateMaterialState() {
        _createMaterialState.value = null
    }

    fun resetDeleteMaterialState() {
        _deleteMaterialState.value = null
    }

    fun resetCreateExamState() {
        _createExamState.value = null
    }

    fun resetDeleteExamState() {
        _deleteExamState.value = null
    }

    // Upload Material with File
    fun uploadMaterial(
        subjectId: RequestBody,
        title: RequestBody,
        description: RequestBody,
        type: RequestBody,
        file: MultipartBody.Part? = null,
        url: RequestBody? = null
    ) {
        viewModelScope.launch {
            _uploadMaterialState.value = Resource.Loading()
            val result = adminRepository.uploadMaterial(subjectId, title, description, type, file, url)
            _uploadMaterialState.value = result
        }
    }

    fun resetUploadMaterialState() {
        _uploadMaterialState.value = null
    }
}
