package com.campushelper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campushelper.app.data.model.Test
import com.campushelper.app.data.model.TestRequest
import com.campushelper.app.data.model.TestResult
import com.campushelper.app.data.model.TestResults
import com.campushelper.app.data.repository.TestRepository
import com.campushelper.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val testRepository: TestRepository
) : ViewModel() {

    private val _testState = MutableStateFlow<Resource<Test>?>(null)
    val testState: StateFlow<Resource<Test>?> = _testState

    private val _submitState = MutableStateFlow<Resource<TestResults>?>(null)
    val submitState: StateFlow<Resource<TestResults>?> = _submitState

    private val _testResult = MutableStateFlow<Resource<TestResult>?>(null)
    val testResult: StateFlow<Resource<TestResult>?> = _testResult

    private val _testsState = MutableStateFlow<Resource<List<Test>>?>(null)
    val testsState: StateFlow<Resource<List<Test>>?> = _testsState

    private val _testAnalysis = MutableStateFlow<Resource<com.campushelper.app.data.model.TestAnalysis>?>(null)
    val testAnalysis: StateFlow<Resource<com.campushelper.app.data.model.TestAnalysis>?> = _testAnalysis

    fun generateTest(subjectId: String, topic: String, questionCount: Int = 10, difficulty: String = "medium") {
        viewModelScope.launch {
            _testState.value = Resource.Loading()
            val request = TestRequest(subjectId, topic, questionCount, difficulty)
            val result = testRepository.generateTest(request)
            _testState.value = result
        }
    }

    fun getTests(status: String? = null, subjectId: String? = null) {
        viewModelScope.launch {
            _testsState.value = Resource.Loading()
            val result = testRepository.getTests(status, subjectId)
            _testsState.value = result
        }
    }

    fun submitTest(testId: String, answers: List<Int>) {
        viewModelScope.launch {
            _submitState.value = Resource.Loading()
            val result = testRepository.submitTest(testId, answers)
            _submitState.value = result
        }
    }

    fun getTestResult(testId: String) {
        viewModelScope.launch {
            _testResult.value = Resource.Loading()
            val result = testRepository.getTestResult(testId)
            _testResult.value = result
        }
    }

    fun getTestAnalysis(testId: String) {
        viewModelScope.launch {
            _testAnalysis.value = Resource.Loading()
            val result = testRepository.getTestAnalysis(testId)
            _testAnalysis.value = result
        }
    }
}
