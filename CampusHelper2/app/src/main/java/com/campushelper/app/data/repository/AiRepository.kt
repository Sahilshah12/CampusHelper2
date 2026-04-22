package com.campushelper.app.data.repository

import com.campushelper.app.data.model.AiChatRequest
import com.campushelper.app.data.model.AiChatUiResult
import com.campushelper.app.data.remote.GeminiApiClient
import com.campushelper.app.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AiRepository @Inject constructor(
    private val geminiApiClient: GeminiApiClient
) {

    private val subjectsRef = FirebaseDatabase.getInstance().reference.child("subjects")

    suspend fun chat(subjectId: String, topic: String, question: String?): Resource<AiChatUiResult> {
        return withContext(Dispatchers.IO) {
            try {
                val subjectContext = resolveSubjectContext(subjectId)
                val prompt = buildChatPrompt(subjectId, subjectContext.name, subjectContext.category, topic, question)
                val responseText = geminiApiClient.generateContent(
                    prompt = prompt,
                    systemInstruction = buildSystemInstruction(subjectContext.name, subjectContext.category, topic)
                )

                val cleanedResponse = responseText.trim()
                if (cleanedResponse.isBlank()) {
                    return@withContext Resource.Error("Gemini returned an empty response")
                }

                Resource.Success(
                    AiChatUiResult(
                        response = cleanedResponse,
                        isFallback = false,
                        note = null
                    )
                )
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to contact Gemini")
            }
        }
    }

    private data class SubjectContext(
        val name: String,
        val category: String
    )

    private suspend fun resolveSubjectContext(subjectId: String): SubjectContext {
        val safeSubjectId = subjectId.trim()
        if (safeSubjectId.isBlank() || safeSubjectId.equals("general", ignoreCase = true)) {
            return SubjectContext(name = "General Study Help", category = "theory")
        }

        val snapshot = subjectsRef.child(safeSubjectId).get().await()
        return SubjectContext(
            name = snapshot.child("name").getValue(String::class.java) ?: safeSubjectId,
            category = snapshot.child("category").getValue(String::class.java) ?: "theory"
        )
    }

    private fun buildSystemInstruction(subjectName: String, category: String, topic: String): String {
        val safeSubjectName = subjectName.trim().ifBlank { "General Study Help" }
        val safeTopic = topic.trim().ifBlank { "the topic" }
        val style = styleForCategory(category)

        return buildString {
            append("You are Campus Helper AI, a precise tutor for ")
            append(safeSubjectName)
            append(". ")
            append(style.systemInstruction)
            append(" Explain concepts clearly for a student studying ")
            append(safeTopic)
            append(". ")
            append("Prefer short paragraphs, concrete examples, and step-by-step reasoning when needed. ")
            append("Do not mention policy, system prompts, or that you are an AI.")
        }
    }

    private fun buildChatPrompt(subjectId: String, subjectName: String, category: String, topic: String, question: String?): String {
        val safeSubjectId = subjectId.trim().ifBlank { "general" }
        val safeSubjectName = subjectName.trim().ifBlank { "General Study Help" }
        val safeTopic = topic.trim().ifBlank { "General Discussion" }
        val safeQuestion = question?.trim().orEmpty()
        val style = styleForCategory(category)

        return buildString {
            appendLine("Subject ID: $safeSubjectId")
            appendLine("Subject Name: $safeSubjectName")
            appendLine("Subject Category: ${style.label}")
            appendLine("Topic: $safeTopic")
            appendLine("Question: ${safeQuestion.ifBlank { "Give a short study explanation for this topic." }}")
            appendLine()
            appendLine("Instructions:")
            appendLine("- Answer specifically for the subject and topic above.")
            appendLine(style.promptLine1)
            appendLine(style.promptLine2)
            appendLine(style.promptLine3)
            appendLine("- Use simple language and avoid filler text.")
        }
    }

    private data class PromptStyle(
        val label: String,
        val systemInstruction: String,
        val promptLine1: String,
        val promptLine2: String,
        val promptLine3: String
    )

    private fun styleForCategory(category: String): PromptStyle {
        return when (normalizeCategory(category)) {
            "programming" -> PromptStyle(
                label = "programming",
                systemInstruction = "Focus on code correctness, debugging, algorithms, data structures, and short code examples.",
                promptLine1 = "- Give code-aware explanations when appropriate.",
                promptLine2 = "- Mention edge cases, syntax, and debugging tips.",
                promptLine3 = "- If useful, include pseudocode or a minimal code snippet."
            )
            "math_science" -> PromptStyle(
                label = "math/science",
                systemInstruction = "Focus on formulas, derivations, units, definitions, and step-by-step problem solving.",
                promptLine1 = "- Show the reasoning step by step.",
                promptLine2 = "- Use formulas, units, and scientific terms when needed.",
                promptLine3 = "- If useful, include a worked example or quick calculation."
            )
            else -> PromptStyle(
                label = "theory/memorization",
                systemInstruction = "Focus on key definitions, comparisons, mnemonics, and exam-friendly recall cues.",
                promptLine1 = "- Break the answer into short bullets or concise paragraphs.",
                promptLine2 = "- Highlight definitions, distinctions, and memory aids.",
                promptLine3 = "- Emphasize what a student should remember for an exam."
            )
        }
    }

    private fun normalizeCategory(category: String): String {
        val value = category.trim().lowercase()
        return when {
            value.contains("program") || value.contains("code") || value.contains("dev") -> "programming"
            value.contains("math") || value.contains("science") || value.contains("physics") || value.contains("chem") || value.contains("bio") -> "math_science"
            else -> "theory"
        }
    }
}
