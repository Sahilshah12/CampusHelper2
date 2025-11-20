package com.campushelper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campushelper.app.data.model.CompetitiveExam
import com.campushelper.app.data.model.EnrollResponse
import com.campushelper.app.data.model.Test
import com.campushelper.app.data.repository.CompetitiveExamRepository
import com.campushelper.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompetitiveExamViewModel @Inject constructor(
    private val examRepository: CompetitiveExamRepository
) : ViewModel() {

    private val _examsState = MutableStateFlow<Resource<List<CompetitiveExam>>?>(null)
    val examsState: StateFlow<Resource<List<CompetitiveExam>>?> = _examsState

    private val _enrollState = MutableStateFlow<Resource<EnrollResponse>?>(null)
    val enrollState: StateFlow<Resource<EnrollResponse>?> = _enrollState

    private val _testState = MutableStateFlow<Resource<Test>?>(null)
    val testState: StateFlow<Resource<Test>?> = _testState

    init {
        loadExams()
    }

    fun loadExams() {
        viewModelScope.launch {
            _examsState.value = Resource.Loading()
            val result = examRepository.getExams()
            _examsState.value = result
        }
    }

    fun enrollInExam(examId: String, dailyTestsCount: Int) {
        viewModelScope.launch {
            _enrollState.value = Resource.Loading()
            val result = examRepository.enrollInExam(examId, dailyTestsCount)
            _enrollState.value = result
        }
    }

    fun generateCompetitiveTest(examId: String) {
        viewModelScope.launch {
            _testState.value = Resource.Loading()
            val result = examRepository.generateCompetitiveTest(examId)
            _testState.value = result
        }
    }
}
