package com.campushelper.app.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.campushelper.app.databinding.ActivityTestResultBinding
import com.campushelper.app.ui.viewmodel.TestViewModel
import com.campushelper.app.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestResultBinding
    private val viewModel: TestViewModel by viewModels()
    private var testId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        testId = intent.getStringExtra("TEST_ID") ?: ""

        setupToolbar()
        setupClickListeners()
        observeViewModel()

        viewModel.getTestResult(testId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Test Results"
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.testResult.collect { resource ->
                when (resource) {
                    null -> {
                        // Initial state
                        binding.progressBar.visibility = View.GONE
                        binding.contentLayout.visibility = View.GONE
                    }
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.contentLayout.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.contentLayout.visibility = View.VISIBLE
                        resource.data?.let { result ->
                            displayResults(result)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@TestResultActivity, resource.message ?: "Error loading results", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun displayResults(result: com.campushelper.app.data.model.TestResult) {
        binding.tvScore.text = "${result.score}%"
        binding.tvCorrectAnswers.text = result.correctAnswers.toString()
        binding.tvIncorrectAnswers.text = result.incorrectAnswers.toString()
        binding.tvTotalQuestions.text = result.totalQuestions.toString()
        
        val grade = when {
            result.score >= 90 -> "A+"
            result.score >= 80 -> "A"
            result.score >= 70 -> "B"
            result.score >= 60 -> "C"
            result.score >= 50 -> "D"
            else -> "F"
        }
        binding.tvGrade.text = grade

        // Set score color based on performance
        val scoreColor = when {
            result.score >= 75 -> android.R.color.holo_green_dark
            result.score >= 50 -> android.R.color.holo_orange_dark
            else -> android.R.color.holo_red_dark
        }
        binding.tvScore.setTextColor(resources.getColor(scoreColor, null))
    }
}
