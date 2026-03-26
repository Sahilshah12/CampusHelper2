package com.campushelper.app.data.model

data class AiChatRequest(
    val subjectId: String,
    val topic: String,
    val question: String?
)

data class AiChatResponse(
    val success: Boolean,
    val response: String,
    val context: ChatContext
)

data class ChatContext(
    val subject: String,
    val topic: String,
    val question: String?
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long
)
