package com.campushelper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campushelper.app.data.repository.AiRepository
import com.campushelper.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _chatState = MutableStateFlow<Resource<String>?>(null)
    val chatState: StateFlow<Resource<String>?> = _chatState

    fun sendMessage(subjectId: String, topic: String, question: String) {
        viewModelScope.launch {
            _chatState.value = Resource.Loading()
            val result = aiRepository.chat(subjectId, topic, question)
            _chatState.value = result
        }
    }
}
