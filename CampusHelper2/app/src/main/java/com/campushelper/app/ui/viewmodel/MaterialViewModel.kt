package com.campushelper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campushelper.app.data.model.Material
import com.campushelper.app.data.repository.MaterialRepository
import com.campushelper.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaterialViewModel @Inject constructor(
    private val materialRepository: MaterialRepository
) : ViewModel() {

    private val _materialsState = MutableStateFlow<Resource<List<Material>>?>(null)
    val materialsState: StateFlow<Resource<List<Material>>?> = _materialsState

    fun loadMaterials() {
        viewModelScope.launch {
            _materialsState.value = Resource.Loading()
            val result = materialRepository.getMaterials()
            _materialsState.value = result
        }
    }

    fun loadMaterialsBySubject(subjectId: String) {
        viewModelScope.launch {
            _materialsState.value = Resource.Loading()
            val result = materialRepository.getMaterialsBySubject(subjectId)
            _materialsState.value = result
        }
    }
}
