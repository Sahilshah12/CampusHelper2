package com.campushelper.app.data.remote

import com.campushelper.app.BuildConfig
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GeminiApiClient @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    private val apiKey: String = BuildConfig.GEMINI_API_KEY.trim()
    private val modelCandidates = listOf(
        "gemini-2.5-flash",
        "gemini-2.0-flash",
        "gemini-1.5-flash-002",
        "gemini-1.5-flash"
    )

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null,
        responseMimeType: String? = null,
        temperature: Double = 0.7,
        maxOutputTokens: Int = 2048
    ): String {
        return withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                throw IllegalStateException("Gemini API key is not configured")
            }

            val payload = JsonObject().apply {
                val contents = JsonArray().apply {
                    add(JsonObject().apply {
                        addProperty("role", "user")
                        add("parts", JsonArray().apply {
                            add(JsonObject().apply {
                                addProperty("text", prompt)
                            })
                        })
                    })
                }
                add("contents", contents)

                if (!systemInstruction.isNullOrBlank()) {
                    add("systemInstruction", JsonObject().apply {
                        add("parts", JsonArray().apply {
                            add(JsonObject().apply {
                                addProperty("text", systemInstruction)
                            })
                        })
                    })
                }

                add("generationConfig", JsonObject().apply {
                    addProperty("temperature", temperature)
                    addProperty("maxOutputTokens", maxOutputTokens)
                    if (!responseMimeType.isNullOrBlank()) {
                        addProperty("responseMimeType", responseMimeType)
                    }
                })
            }

            var lastError: String? = null

            for (model in modelCandidates) {
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
                    .post(payload.toString().toRequestBody("application/json".toMediaType()))
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    return@withContext extractText(responseBody)
                }

                val errorMessage = extractErrorMessage(responseBody) ?: "Gemini request failed with code ${response.code}"
                lastError = errorMessage

                val shouldTryNext = response.code == 404 || errorMessage.contains("NOT_FOUND", ignoreCase = true)
                if (!shouldTryNext) {
                    throw IllegalStateException(errorMessage)
                }
            }

            throw IllegalStateException(
                lastError?.let { "Gemini models unavailable for this API key: $it" }
                    ?: "Gemini models unavailable for this API key"
            )
        }
    }

    private fun extractText(rawBody: String): String {
        if (rawBody.isBlank()) {
            throw IllegalStateException("Gemini response was empty")
        }

        val root = JsonParser.parseString(rawBody).asJsonObject
        val candidates = root.getAsJsonArray("candidates")
            ?: throw IllegalStateException("Gemini response did not contain candidates")
        if (candidates.size() == 0) {
            throw IllegalStateException("Gemini response did not contain a usable candidate")
        }

        val firstCandidate = candidates[0].asJsonObject
            ?: throw IllegalStateException("Gemini response did not contain a usable candidate")
        val content = firstCandidate.getAsJsonObject("content")
            ?: throw IllegalStateException("Gemini response did not contain content")
        val parts = content.getAsJsonArray("parts")
            ?: throw IllegalStateException("Gemini response did not contain parts")

        val text = parts.mapNotNull { partElement ->
            val partObject = partElement.asJsonObject
            if (partObject.has("text") && !partObject.get("text").isJsonNull) {
                runCatching { partObject.get("text").asString }.getOrNull()
            } else {
                null
            }
        }.joinToString(separator = "\n").trim()

        if (text.isBlank()) {
            throw IllegalStateException("Gemini returned an empty text payload")
        }

        return text
    }

    private fun extractErrorMessage(rawBody: String): String? {
        return runCatching {
            val root = JsonParser.parseString(rawBody).asJsonObject
            when {
                root.has("error") && root.get("error").isJsonObject -> {
                    val errorObject = root.getAsJsonObject("error")
                    val message = errorObject.get("message")?.asString.orEmpty()
                    val status = errorObject.get("status")?.asString.orEmpty()
                    if (status.isNotBlank()) "$status: $message" else message
                }
                root.has("message") -> root.get("message").asString
                else -> null
            }
        }.getOrNull()
    }
}
