package com.campushelper.app.data.repository

import com.campushelper.app.data.model.Progress
import com.campushelper.app.data.model.Streak
import com.campushelper.app.data.model.SubjectInfo
import com.campushelper.app.data.model.SubjectProgress
import com.campushelper.app.utils.SessionManager
import com.campushelper.app.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ProgressRepository @Inject constructor(
    private val sessionManager: SessionManager
) {

    private val auth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val testsRef = firebaseDatabase.reference.child("tests")
    private val subjectsRef = firebaseDatabase.reference.child("subjects")

    suspend fun getProgress(): Resource<Progress> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: sessionManager.getUserId()
                    ?: return@withContext Resource.Error("User not logged in")

                val subjectSnapshot = subjectsRef.get().await()
                val subjectsById = subjectSnapshot.children.associate { snap ->
                    val id = snap.child("_id").getValue(String::class.java) ?: snap.key.orEmpty()
                    val name = snap.child("name").getValue(String::class.java) ?: "Unknown"
                    val code = snap.child("code").getValue(String::class.java) ?: "N/A"
                    id to SubjectInfo(_id = id, name = name, code = code)
                }

                val testsSnapshot = testsRef.child(userId).get().await()
                val completedTests = testsSnapshot.children.filter {
                    it.child("status").getValue(String::class.java) == "completed"
                }

                val totalTests = completedTests.size
                val totalQuestionsAttempted = completedTests.sumOf {
                    it.child("totalQuestions").getValue(Int::class.java) ?: 0
                }
                val totalCorrectAnswers = completedTests.sumOf {
                    it.child("correctAnswers").getValue(Int::class.java) ?: 0
                }

                val averageScore = if (totalQuestionsAttempted > 0) {
                    (totalCorrectAnswers.toDouble() / totalQuestionsAttempted.toDouble()) * 100.0
                } else {
                    0.0
                }

                val groupedBySubject = completedTests.groupBy {
                    it.child("subjectId").getValue(String::class.java).orEmpty()
                }.filterKeys { it.isNotBlank() }

                val subjectWiseProgress = groupedBySubject.map { (subjectId, tests) ->
                    val subjectInfo = subjectsById[subjectId] ?: SubjectInfo(
                        _id = subjectId,
                        name = "Unknown Subject",
                        code = "N/A"
                    )

                    val subjectTotalQuestions = tests.sumOf {
                        it.child("totalQuestions").getValue(Int::class.java) ?: 0
                    }
                    val subjectCorrectAnswers = tests.sumOf {
                        it.child("correctAnswers").getValue(Int::class.java) ?: 0
                    }
                    val subjectAccuracy = if (subjectTotalQuestions > 0) {
                        (subjectCorrectAnswers.toDouble() / subjectTotalQuestions.toDouble()) * 100.0
                    } else {
                        0.0
                    }

                    SubjectProgress(
                        subjectId = subjectInfo,
                        testsAttempted = tests.size,
                        totalQuestions = subjectTotalQuestions,
                        correctAnswers = subjectCorrectAnswers,
                        accuracy = subjectAccuracy
                    )
                }

                val completedAtList = completedTests.mapNotNull {
                    it.child("completedAt").getValue(Long::class.java)
                }.sorted()

                val streak = calculateStreak(completedAtList)

                Resource.Success(
                    Progress(
                        totalTests = totalTests,
                        totalQuestionsAttempted = totalQuestionsAttempted,
                        totalCorrectAnswers = totalCorrectAnswers,
                        averageScore = averageScore,
                        subjectWiseProgress = subjectWiseProgress,
                        streak = streak
                    )
                )
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    private fun calculateStreak(completedAtList: List<Long>): Streak {
        if (completedAtList.isEmpty()) {
            return Streak(current = 0, longest = 0, lastActivity = null)
        }

        val dayMs = 24L * 60L * 60L * 1000L
        val days = completedAtList.map { it / dayMs }.distinct().sorted()

        var longest = 1
        var run = 1
        for (i in 1 until days.size) {
            if (days[i] == days[i - 1] + 1) {
                run += 1
                if (run > longest) longest = run
            } else {
                run = 1
            }
        }

        val todayDay = System.currentTimeMillis() / dayMs
        val lastDay = days.last()
        val current = when {
            lastDay == todayDay || lastDay == todayDay - 1 -> {
                var currentRun = 1
                for (i in days.size - 2 downTo 0) {
                    if (days[i + 1] == days[i] + 1) currentRun += 1 else break
                }
                currentRun
            }
            else -> 0
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val lastActivity = formatter.format(Date(completedAtList.last()))

        return Streak(current = current, longest = longest, lastActivity = lastActivity)
    }
}
