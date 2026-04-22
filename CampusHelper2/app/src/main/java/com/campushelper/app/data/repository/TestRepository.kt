package com.campushelper.app.data.repository

import com.campushelper.app.data.model.*
import com.campushelper.app.data.remote.GeminiApiClient
import com.campushelper.app.utils.SessionManager
import com.campushelper.app.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.random.Random
import javax.inject.Inject

class TestRepository @Inject constructor(
    private val sessionManager: SessionManager,
    private val geminiApiClient: GeminiApiClient
) {

    private val auth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val testsRef = firebaseDatabase.reference.child("tests")
    private val subjectsRef = firebaseDatabase.reference.child("subjects")

    suspend fun generateTest(request: TestRequest): Resource<Test> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: sessionManager.getUserId()
                    ?: return@withContext Resource.Error("User not logged in")

                val testId = testsRef.child(userId).push().key
                    ?: return@withContext Resource.Error("Unable to create test")

                val resolvedSubjectId = if (request.subjectId.equals("general", ignoreCase = true)) {
                    null
                } else {
                    request.subjectId
                }

                val subjectSnapshot = resolvedSubjectId?.let { subjectId ->
                    val subjectSnap = subjectsRef.child(subjectId).get().await()
                    SubjectSnapshot(
                        name = subjectSnap.child("name").getValue(String::class.java),
                        category = subjectSnap.child("category").getValue(String::class.java)
                    )
                }

                val questions = generateQuestions(
                    subjectName = subjectSnapshot?.name ?: request.subjectId,
                    subjectCategory = subjectSnapshot?.category,
                    topic = request.topic,
                    count = request.questionCount,
                    difficulty = request.difficulty
                )

                val test = Test(
                    _id = testId,
                    userId = userId,
                    subjectId = resolvedSubjectId,
                    title = buildString {
                        if (!subjectSnapshot?.name.isNullOrBlank()) append("${subjectSnapshot?.name} - ")
                        append("${request.topic} Practice Test")
                    },
                    topic = request.topic,
                    questions = questions,
                    userAnswers = emptyList(),
                    score = 0.0,
                    totalQuestions = questions.size,
                    correctAnswers = 0,
                    status = "pending",
                    createdAt = System.currentTimeMillis(),
                    completedAt = null
                )

                testsRef.child(userId).child(testId).setValue(test).await()
                Resource.Success(test)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getTests(status: String? = null, subjectId: String? = null): Resource<List<Test>> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: sessionManager.getUserId()
                    ?: return@withContext Resource.Error("User not logged in")

                val snapshot = testsRef.child(userId).get().await()
                val tests = snapshot.children.mapNotNull { mapTest(it) }
                    .filter {
                        (status == null || it.status.equals(status, ignoreCase = true)) &&
                            (subjectId == null || it.subjectId == subjectId)
                    }
                    .sortedByDescending { it.createdAt ?: 0L }

                Resource.Success(tests)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun submitTest(testId: String, answers: List<Int>): Resource<TestResults> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: sessionManager.getUserId()
                    ?: return@withContext Resource.Error("User not logged in")

                val testSnapshot = testsRef.child(userId).child(testId).get().await()
                val test = mapTest(testSnapshot)
                    ?: return@withContext Resource.Error("Test not found")

                val normalizedAnswers = answers.take(test.totalQuestions)
                var correctAnswers = 0
                test.questions.forEachIndexed { index, question ->
                    val answerIndex = normalizedAnswers.getOrNull(index) ?: -1
                    if (answerIndex >= 0) {
                        val answerLetter = indexToLetter(answerIndex)
                        if (answerLetter.equals(question.correctAnswer, ignoreCase = true)) {
                            correctAnswers += 1
                        }
                    }
                }

                val score = if (test.totalQuestions > 0) {
                    (correctAnswers.toDouble() / test.totalQuestions.toDouble()) * 100.0
                } else {
                    0.0
                }

                val completedAt = System.currentTimeMillis()
                val updatedTest = test.copy(
                    userAnswers = normalizedAnswers,
                    score = score,
                    correctAnswers = correctAnswers,
                    status = "completed",
                    completedAt = completedAt
                )

                testsRef.child(userId).child(testId).setValue(updatedTest).await()

                Resource.Success(
                    TestResults(
                        score = score,
                        correctAnswers = correctAnswers,
                        totalQuestions = test.totalQuestions,
                        percentage = String.format(Locale.US, "%.2f", score)
                    )
                )
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getTest(testId: String): Resource<Test> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: sessionManager.getUserId()
                    ?: return@withContext Resource.Error("User not logged in")

                val snapshot = testsRef.child(userId).child(testId).get().await()
                val test = mapTest(snapshot)
                    ?: return@withContext Resource.Error("Test not found")

                Resource.Success(test)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getTestResult(testId: String): Resource<TestResult> {
        return withContext(Dispatchers.IO) {
            try {
                val testResult = when (val testResource = getTest(testId)) {
                    is Resource.Success -> {
                        val test = testResource.data
                            ?: return@withContext Resource.Error("Test not found")
                        val mappedUserAnswers = test.userAnswers.mapIndexed { index, answerIndex ->
                            val key = test.questions.getOrNull(index)?.id ?: "q_${index + 1}"
                            key to indexToLetter(answerIndex)
                        }.toMap()

                        TestResult(
                            testId = test._id,
                            subjectId = test.subjectId ?: "general",
                            score = test.score,
                            totalQuestions = test.totalQuestions,
                            correctAnswers = test.correctAnswers,
                            incorrectAnswers = (test.totalQuestions - test.correctAnswers).coerceAtLeast(0),
                            completedAt = test.completedAt ?: System.currentTimeMillis(),
                            questions = test.questions,
                            userAnswers = mappedUserAnswers
                        )
                    }
                    is Resource.Error -> return@withContext Resource.Error(testResource.message ?: "Failed to fetch test result")
                    else -> return@withContext Resource.Error("Failed to fetch test result")
                }

                Resource.Success(testResult)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getTestAnalysis(testId: String): Resource<TestAnalysis> {
        return withContext(Dispatchers.IO) {
            try {
                val test = when (val testResource = getTest(testId)) {
                    is Resource.Success -> testResource.data
                        ?: return@withContext Resource.Error("Test not found")
                    is Resource.Error -> return@withContext Resource.Error(testResource.message ?: "Failed to fetch test analysis")
                    else -> return@withContext Resource.Error("Failed to fetch test analysis")
                }

                val correct = test.correctAnswers
                val total = test.totalQuestions
                val scorePercentage = if (total > 0) ((correct.toDouble() / total.toDouble()) * 100.0).toInt() else 0

                val easyTotal = total / 3
                val mediumTotal = total / 3
                val hardTotal = total - easyTotal - mediumTotal
                val easyCorrect = (correct * easyTotal) / total.coerceAtLeast(1)
                val mediumCorrect = (correct * mediumTotal) / total.coerceAtLeast(1)
                val hardCorrect = correct - easyCorrect - mediumCorrect

                val difficulty = DifficultyBreakdown(
                    easy = DifficultyStats(easyCorrect, easyTotal),
                    medium = DifficultyStats(mediumCorrect, mediumTotal),
                    hard = DifficultyStats(hardCorrect.coerceAtLeast(0), hardTotal.coerceAtLeast(0))
                )

                val reviews = test.questions.mapIndexed { index, q ->
                    QuestionReview(
                        question = q.question,
                        options = listOf(
                            q.options["A"].orEmpty(),
                            q.options["B"].orEmpty(),
                            q.options["C"].orEmpty(),
                            q.options["D"].orEmpty()
                        ),
                        correctAnswer = letterToIndex(q.correctAnswer),
                        userAnswer = test.userAnswers.getOrNull(index),
                        difficulty = when {
                            index < easyTotal -> "easy"
                            index < easyTotal + mediumTotal -> "medium"
                            else -> "hard"
                        }
                    )
                }

                Resource.Success(
                    TestAnalysis(
                        _id = test._id,
                        subjectName = test.title.substringBefore(" - ", "General Test"),
                        topic = test.topic,
                        scorePercentage = scorePercentage,
                        correctAnswers = test.correctAnswers,
                        totalQuestions = test.totalQuestions,
                        totalTimeSeconds = (((test.completedAt ?: System.currentTimeMillis()) - (test.createdAt
                            ?: System.currentTimeMillis())) / 1000L).toInt().coerceAtLeast(0),
                        difficultyBreakdown = difficulty,
                        aiAnalysis = null,
                        questions = reviews
                    )
                )
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    private suspend fun generateQuestions(subjectName: String, subjectCategory: String?, topic: String, count: Int, difficulty: String): List<Question> {
        val geminiQuestions = runCatching {
            generateQuestionsWithGemini(subjectName, subjectCategory, topic, count, difficulty)
        }.getOrNull()

        if (!geminiQuestions.isNullOrEmpty()) {
            return geminiQuestions
        }

        return generateLocalQuestions(subjectName, subjectCategory, topic, count, difficulty)
    }

    private suspend fun generateQuestionsWithGemini(subjectName: String, subjectCategory: String?, topic: String, count: Int, difficulty: String): List<Question>? {
        val safeCount = count.coerceIn(5, 50)
        val safeSubjectName = subjectName.ifBlank { "General Study Help" }
        val style = styleForCategory(subjectCategory)
        val focus = topic.ifBlank { "General Concepts" }

        val prompt = buildString {
            appendLine("Create exactly $safeCount unique multiple-choice questions for the subject and topic below.")
            appendLine("Return only valid JSON and nothing else.")
            appendLine()
            appendLine("Subject: $safeSubjectName")
            appendLine("Subject Category: ${style.label}")
            appendLine("Topic: $focus")
            appendLine("Difficulty: $difficulty")
            appendLine()
            appendLine("JSON format:")
            appendLine("{")
            appendLine("  \"questions\": [")
            appendLine("    {")
            appendLine("      \"id\": \"q_1\",")
            appendLine("      \"question\": \"...\",")
            appendLine("      \"options\": {\"A\": \"...\", \"B\": \"...\", \"C\": \"...\", \"D\": \"...\"},")
            appendLine("      \"correctAnswer\": \"A\",")
            appendLine("      \"explanation\": \"...\"")
            appendLine("    }")
            appendLine("  ]")
            appendLine("}")
            appendLine()
            appendLine("Rules:")
            appendLine("- Every question must be different and grounded in the subject above.")
            appendLine("- All four options must be unique.")
            appendLine("- correctAnswer must be one of A, B, C, or D.")
            appendLine("- Explanation should be one sentence.")
            appendLine(style.extraPromptRule)
        }

        val responseText = geminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = "You generate educational multiple-choice questions for an Android learning app.",
            responseMimeType = "application/json",
            temperature = 0.9,
            maxOutputTokens = 4096
        )

        val jsonText = extractJsonPayload(responseText)
        val root = JsonParser.parseString(jsonText).asJsonObject
        val questionsArray = root.getAsJsonArray("questions") ?: return null

        val parsedQuestions = questionsArray.mapNotNullIndexed { index, element ->
            val item = element.asJsonObject
            val questionText = item.get("question")?.asString?.trim().orEmpty()
            val optionsObject = item.getAsJsonObject("options") ?: return@mapNotNullIndexed null
            val options = linkedMapOf(
                "A" to optionsObject.get("A")?.asString.orEmpty(),
                "B" to optionsObject.get("B")?.asString.orEmpty(),
                "C" to optionsObject.get("C")?.asString.orEmpty(),
                "D" to optionsObject.get("D")?.asString.orEmpty()
            )
            val correctAnswer = item.get("correctAnswer")?.asString?.trim()?.uppercase().orEmpty()
            val explanation = item.get("explanation")?.asString?.trim()

            if (questionText.isBlank() || options.values.any { it.isBlank() } || correctAnswer !in setOf("A", "B", "C", "D")) {
                return@mapNotNullIndexed null
            }

            Question(
                id = item.get("id")?.asString?.trim().orEmpty().ifBlank { "q_${index + 1}" },
                question = questionText,
                options = options,
                correctAnswer = correctAnswer,
                explanation = explanation
            )
        }

        return parsedQuestions.takeIf { it.isNotEmpty() }
    }

    private fun generateLocalQuestions(subjectName: String, subjectCategory: String?, topic: String, count: Int, difficulty: String): List<Question> {
        val safeCount = count.coerceIn(5, 50)
        val safeSubjectName = subjectName.ifBlank { "General Study Help" }
        val style = styleForCategory(subjectCategory)
        val focus = topic.ifBlank { "General Concepts" }
        val diffPrefix = when (difficulty.lowercase()) {
            "easy" -> "Basic"
            "hard" -> "Advanced"
            else -> "Core"
        }

        val random = Random(System.currentTimeMillis() xor focus.hashCode().toLong())

        val stems = style.localStems
        val distractors = style.localDistractors

        return (1..safeCount).map { idx ->
            val keyConcept = "$diffPrefix concept ${random.nextInt(1, 100)}"
            val stem = stems.random(random)
            val questionText = "In $safeSubjectName, $focus: $stem $keyConcept?"

            val correctStatement = buildString {
                append(keyConcept)
                append(" is applied to solve typical ")
                append(safeSubjectName)
                append(" and ")
                append(focus)
                append(" problems by focusing on the underlying rule rather than a memorized shortcut.")
            }

            val wrongStatements = distractors.shuffled(random).take(3).map { suffix ->
                "$keyConcept $suffix"
            }

            val optionPool = mutableListOf(
                correctStatement,
                wrongStatements[0],
                wrongStatements[1],
                wrongStatements[2]
            )

            optionPool.shuffle(random)

            val letters = listOf("A", "B", "C", "D")
            val options = linkedMapOf<String, String>()
            var correctLetter = "A"
            optionPool.forEachIndexed { optionIndex, option ->
                val letter = letters[optionIndex]
                options[letter] = option
                if (option == correctStatement) {
                    correctLetter = letter
                }
            }

            Question(
                id = "q_$idx",
                question = questionText,
                options = options,
                correctAnswer = correctLetter,
                explanation = "Review why $correctStatement"
            )
        }
    }

    private fun extractJsonPayload(rawText: String): String {
        val trimmed = rawText.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed
        }

        val fenceMatch = Regex("```(?:json)?\\s*(.*?)\\s*```", RegexOption.DOT_MATCHES_ALL)
            .find(trimmed)
        return fenceMatch?.groupValues?.getOrNull(1)?.trim().orEmpty().ifBlank { trimmed }
    }

    private data class SubjectSnapshot(
        val name: String?,
        val category: String?
    )

    private data class QuestionStyle(
        val label: String,
        val extraPromptRule: String,
        val localStems: List<String>,
        val localDistractors: List<String>
    )

    private fun styleForCategory(category: String?): QuestionStyle {
        val value = category?.trim()?.lowercase().orEmpty()
        return when {
            value.contains("program") || value.contains("code") || value.contains("dev") -> QuestionStyle(
                label = "programming",
                extraPromptRule = "- Prefer questions about debugging, algorithm tracing, syntax, or output prediction.",
                localStems = listOf(
                    "Which output is produced by",
                    "What is the best explanation of",
                    "Which fix is most appropriate for",
                    "How would you debug",
                    "Which statement about"
                ),
                localDistractors = listOf(
                    "is unrelated to the runtime behavior.",
                    "would cause a compilation error.",
                    "does not follow the algorithm's logic.",
                    "is a misconception about the code.",
                    "would not solve the programming issue."
                )
            )
            value.contains("math") || value.contains("science") || value.contains("physics") || value.contains("chem") || value.contains("bio") -> QuestionStyle(
                label = "math_science",
                extraPromptRule = "- Prefer formulas, steps, units, calculations, or scientific reasoning.",
                localStems = listOf(
                    "Which formula best applies to",
                    "What is the correct calculation for",
                    "Which scientific principle explains",
                    "How should you solve",
                    "Which measurement is most relevant for"
                ),
                localDistractors = listOf(
                    "does not match the required units.",
                    "ignores the relevant formula.",
                    "is scientifically inaccurate.",
                    "does not follow the expected derivation.",
                    "is not supported by the data."
                )
            )
            else -> QuestionStyle(
                label = "theory/memorization",
                extraPromptRule = "- Prefer recall, definitions, comparisons, and exam-friendly memory cues.",
                localStems = listOf(
                    "Which statement best defines",
                    "What is the key difference between",
                    "Which description is most accurate for",
                    "How would you summarize",
                    "Which fact is most important about"
                ),
                localDistractors = listOf(
                    "does not match the standard definition.",
                    "is not a correct comparison.",
                    "confuses two separate concepts.",
                    "is too vague to be useful.",
                    "is not the exam-relevant idea."
                )
            )
        }
    }

    private inline fun <T, R> Iterable<T>.mapNotNullIndexed(transform: (index: Int, T) -> R?): List<R> {
        val result = ArrayList<R>()
        var index = 0
        for (item in this) {
            val mapped = transform(index, item)
            if (mapped != null) {
                result.add(mapped)
            }
            index++
        }
        return result
    }

    private fun mapTest(snapshot: com.google.firebase.database.DataSnapshot): Test? {
        val id = snapshot.child("_id").getValue(String::class.java)
            ?: snapshot.key
            ?: return null
        val userId = snapshot.child("userId").getValue(String::class.java) ?: return null
        val title = snapshot.child("title").getValue(String::class.java) ?: return null
        val topic = snapshot.child("topic").getValue(String::class.java)
        val questions = snapshot.child("questions").children.mapNotNull { qSnap ->
            val qId = qSnap.child("id").getValue(String::class.java) ?: qSnap.key ?: return@mapNotNull null
            val qText = qSnap.child("question").getValue(String::class.java) ?: return@mapNotNull null
            val optionsNode = qSnap.child("options")
            val options = linkedMapOf(
                "A" to (optionsNode.child("A").getValue(String::class.java) ?: "Option A"),
                "B" to (optionsNode.child("B").getValue(String::class.java) ?: "Option B"),
                "C" to (optionsNode.child("C").getValue(String::class.java) ?: "Option C"),
                "D" to (optionsNode.child("D").getValue(String::class.java) ?: "Option D")
            )

            Question(
                id = qId,
                question = qText,
                options = options,
                correctAnswer = qSnap.child("correctAnswer").getValue(String::class.java) ?: "A",
                explanation = qSnap.child("explanation").getValue(String::class.java)
            )
        }

        val userAnswers = snapshot.child("userAnswers").children.mapNotNull { it.getValue(Int::class.java) }

        return Test(
            _id = id,
            userId = userId,
            subjectId = snapshot.child("subjectId").getValue(String::class.java),
            title = title,
            topic = topic,
            questions = questions,
            userAnswers = userAnswers,
            score = snapshot.child("score").getValue(Double::class.java) ?: 0.0,
            totalQuestions = snapshot.child("totalQuestions").getValue(Int::class.java) ?: questions.size,
            correctAnswers = snapshot.child("correctAnswers").getValue(Int::class.java) ?: 0,
            status = snapshot.child("status").getValue(String::class.java) ?: "pending",
            createdAt = snapshot.child("createdAt").getValue(Long::class.java),
            completedAt = snapshot.child("completedAt").getValue(Long::class.java)
        )
    }

    private fun indexToLetter(index: Int): String = when (index) {
        0 -> "A"
        1 -> "B"
        2 -> "C"
        3 -> "D"
        else -> "A"
    }

    private fun letterToIndex(letter: String?): Int = when (letter?.uppercase()) {
        "A" -> 0
        "B" -> 1
        "C" -> 2
        "D" -> 3
        else -> 0
    }
}
