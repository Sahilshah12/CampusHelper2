package com.campushelper.app.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.campushelper.app.databinding.ActivityTestAnalysisBinding
import com.campushelper.app.ui.adapter.QuestionReviewAdapter
import com.campushelper.app.ui.viewmodel.TestViewModel
import com.campushelper.app.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestAnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestAnalysisBinding
    private val viewModel: TestViewModel by viewModels()
    private lateinit var questionAdapter: QuestionReviewAdapter
    private var testId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        testId = intent.getStringExtra("TEST_ID") ?: ""
        
        if (testId.isEmpty()) {
            Toast.makeText(this, "Invalid test ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        setupRecyclerView()
        observeViewModel()
        loadTestDetails()
    }

    private fun setupViews() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        questionAdapter = QuestionReviewAdapter()
        binding.rvQuestions.apply {
            layoutManager = LinearLayoutManager(this@TestAnalysisActivity)
            adapter = questionAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.testAnalysis.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.scrollView.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.scrollView.visibility = View.VISIBLE
                        resource.data?.let { analysis ->
                            displayTestAnalysis(analysis)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@TestAnalysisActivity,
                            resource.message ?: "Failed to load analysis",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    null -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun loadTestDetails() {
        viewModel.getTestAnalysis(testId)
    }

    private fun displayTestAnalysis(analysis: com.campushelper.app.data.model.TestAnalysis) {
        // Quiz Overview
        binding.tvQuizTitle.text = analysis.subjectName
        binding.tvTopicName.text = analysis.topic ?: "General Test"
        
        val scorePercentage = analysis.scorePercentage
        binding.tvScore.text = "$scorePercentage%"
        binding.tvCorrectAnswers.text = "${analysis.correctAnswers}/${analysis.totalQuestions}"
        binding.progressScore.progress = scorePercentage
        
        // Update score color based on percentage
        val scoreColor = when {
            scorePercentage >= 80 -> getColor(com.campushelper.app.R.color.success)
            scorePercentage >= 60 -> getColor(com.campushelper.app.R.color.warning)
            else -> getColor(com.campushelper.app.R.color.error)
        }
        binding.tvScore.setTextColor(scoreColor)
        
        // Time Stats
        val totalMinutes = analysis.totalTimeSeconds / 60
        val totalSeconds = analysis.totalTimeSeconds % 60
        binding.tvTotalTime.text = if (totalMinutes > 0) {
            "$totalMinutes min ${totalSeconds}s"
        } else {
            "${totalSeconds}s"
        }
        
        val avgTime = if (analysis.totalQuestions > 0) {
            analysis.totalTimeSeconds / analysis.totalQuestions
        } else {
            0
        }
        binding.tvAverageTime.text = "${avgTime}s per question"
        
        // Difficulty Breakdown
        if (analysis.difficultyBreakdown != null) {
            binding.cardDifficulty.visibility = View.VISIBLE
            displayDifficultyBreakdown(analysis.difficultyBreakdown)
        } else {
            binding.cardDifficulty.visibility = View.GONE
        }
        
        // AI Analysis
        if (analysis.aiAnalysis != null) {
            binding.cardAIAnalysis.visibility = View.VISIBLE
            displayAIAnalysis(analysis.aiAnalysis)
        } else {
            binding.cardAIAnalysis.visibility = View.GONE
        }
        
        // Questions Review
        questionAdapter.submitList(analysis.questions)
    }

    private fun displayDifficultyBreakdown(breakdown: com.campushelper.app.data.model.DifficultyBreakdown) {
        // Easy
        if (breakdown.easy.total > 0) {
            val easyPercentage = (breakdown.easy.correct * 100) / breakdown.easy.total
            binding.tvEasyScore.text = "$easyPercentage%"
            binding.tvEasyQuestions.text = "(${breakdown.easy.correct}/${breakdown.easy.total})"
            binding.progressEasy.progress = easyPercentage
        } else {
            binding.tvEasyScore.text = "N/A"
            binding.tvEasyQuestions.text = "(0/0)"
            binding.progressEasy.progress = 0
        }
        
        // Medium
        if (breakdown.medium.total > 0) {
            val mediumPercentage = (breakdown.medium.correct * 100) / breakdown.medium.total
            binding.tvMediumScore.text = "$mediumPercentage%"
            binding.tvMediumQuestions.text = "(${breakdown.medium.correct}/${breakdown.medium.total})"
            binding.progressMedium.progress = mediumPercentage
        } else {
            binding.tvMediumScore.text = "N/A"
            binding.tvMediumQuestions.text = "(0/0)"
            binding.progressMedium.progress = 0
        }
        
        // Hard
        if (breakdown.hard.total > 0) {
            val hardPercentage = (breakdown.hard.correct * 100) / breakdown.hard.total
            binding.tvHardScore.text = "$hardPercentage%"
            binding.tvHardQuestions.text = "(${breakdown.hard.correct}/${breakdown.hard.total})"
            binding.progressHard.progress = hardPercentage
        } else {
            binding.tvHardScore.text = "N/A"
            binding.tvHardQuestions.text = "(0/0)"
            binding.progressHard.progress = 0
        }
    }

    private fun displayAIAnalysis(aiAnalysis: com.campushelper.app.data.model.AIAnalysis) {
        binding.tvOverallAssessment.text = aiAnalysis.overallAssessment
        
        // Strengths
        val strengthsText = aiAnalysis.strengths.joinToString("\n") { "• $it" }
        binding.tvStrengths.text = strengthsText.ifEmpty { "• No specific strengths identified" }
        
        // Weaknesses
        val weaknessesText = aiAnalysis.weaknesses.joinToString("\n") { "• $it" }
        binding.tvWeaknesses.text = weaknessesText.ifEmpty { "• Great job! Keep practicing" }
        
        // Recommendations
        val recommendationsText = aiAnalysis.recommendations.joinToString("\n") { "• $it" }
        binding.tvRecommendations.text = recommendationsText.ifEmpty { "• Continue with your current approach" }
        
        // Study Plan
        val studyPlanText = aiAnalysis.studyPlan.joinToString("\n") { "🎯 $it" }
        binding.tvStudyPlan.text = studyPlanText.ifEmpty { "🎯 Keep practicing regularly" }
    }
}
