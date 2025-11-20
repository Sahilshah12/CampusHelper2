package com.campushelper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campushelper.app.data.model.Material
import com.campushelper.app.data.model.Subject
import com.campushelper.app.data.repository.SubjectRepository
import com.campushelper.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val _subjectsState = MutableStateFlow<Resource<List<Subject>>?>(null)
    val subjectsState: StateFlow<Resource<List<Subject>>?> = _subjectsState

    private val _materials = MutableStateFlow<Resource<List<Material>>?>(null)
    val materials: StateFlow<Resource<List<Material>>?> = _materials

    init {
        loadSubjects()
    }

    fun loadSubjects() {
        viewModelScope.launch {
            _subjectsState.value = Resource.Loading()
            val result = subjectRepository.getSubjects()
            _subjectsState.value = result
        }
    }

    fun getMaterials(subjectId: String) {
        viewModelScope.launch {
            _materials.value = Resource.Loading()
            val result = subjectRepository.getMaterials(subjectId)
            _materials.value = result
        }
    }
}
