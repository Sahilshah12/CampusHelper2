package com.campushelper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campushelper.app.data.model.Progress
import com.campushelper.app.data.repository.ProgressRepository
import com.campushelper.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _progressState = MutableStateFlow<Resource<Progress>>(Resource.Loading())
    val progressState: StateFlow<Resource<Progress>> = _progressState

    init {
        loadProgress()
    }

    fun loadProgress() {
        viewModelScope.launch {
            _progressState.value = Resource.Loading()
            val result = progressRepository.getProgress()
            _progressState.value = result
        }
    }
}
