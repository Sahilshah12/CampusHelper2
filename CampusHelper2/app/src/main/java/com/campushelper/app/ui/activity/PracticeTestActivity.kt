package com.campushelper.app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.campushelper.app.databinding.ActivityPracticeTestBinding
import com.campushelper.app.data.model.Question
import com.campushelper.app.ui.viewmodel.TestViewModel
import com.campushelper.app.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PracticeTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPracticeTestBinding
    private val viewModel: TestViewModel by viewModels()
    private var subjectId: String = ""
    private var topic: String = ""
    private var testId: String = ""
    private var currentQuestions: List<Question> = emptyList()
    private var currentQuestionIndex = 0
    private val userAnswers = mutableMapOf<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPracticeTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subjectId = intent.getStringExtra("SUBJECT_ID") ?: ""
        topic = intent.getStringExtra("TOPIC") ?: ""

        // Log for debugging
        android.util.Log.d("PracticeTest", "SubjectID: $subjectId, Topic: $topic")

        if (topic.isEmpty()) {
            Toast.makeText(this, "Topic is required", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupClickListeners()
        observeViewModel()

        viewModel.generateTest(subjectId, topic)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Practice Test"
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        // Click listeners for cards to toggle radio buttons
        binding.cardOption1.setOnClickListener { 
            saveCurrentAnswer("A")
            binding.rbOption1.isChecked = true
        }
        binding.cardOption2.setOnClickListener { 
            saveCurrentAnswer("B")
            binding.rbOption2.isChecked = true
        }
        binding.cardOption3.setOnClickListener { 
            saveCurrentAnswer("C")
            binding.rbOption3.isChecked = true
        }
        binding.cardOption4.setOnClickListener { 
            saveCurrentAnswer("D")
            binding.rbOption4.isChecked = true
        }

        binding.btnPrevious.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                displayQuestion()
            }
        }

        binding.btnNext.setOnClickListener {
            if (currentQuestionIndex < currentQuestions.size - 1) {
                currentQuestionIndex++
                displayQuestion()
            }
        }

        binding.btnSubmit.setOnClickListener {
            submitTest()
        }
        
        // Setup radio button click listeners
        binding.rbOption1.setOnClickListener {
            saveCurrentAnswer("A")
            binding.rbOption1.isChecked = true
        }
        binding.rbOption2.setOnClickListener {
            saveCurrentAnswer("B")
            binding.rbOption2.isChecked = true
        }
        binding.rbOption3.setOnClickListener {
            saveCurrentAnswer("C")
            binding.rbOption3.isChecked = true
        }
        binding.rbOption4.setOnClickListener {
            saveCurrentAnswer("D")
            binding.rbOption4.isChecked = true
        }
    }

    private fun saveCurrentAnswer(answer: String) {
        if (currentQuestions.isNotEmpty()) {
            userAnswers[currentQuestionIndex] = answer
            android.util.Log.d("PracticeTest", "Saved answer for Q${currentQuestionIndex + 1}: $answer")
            android.util.Log.d("PracticeTest", "Total answers: ${userAnswers.size}/${currentQuestions.size}")
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.testState.collect { resource ->
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
                        resource.data?.let { test ->
                            testId = test._id
                            currentQuestions = test.questions
                            displayQuestion()
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@PracticeTestActivity, resource.message, Toast.LENGTH_LONG).show()
                        finish() // Close activity on error
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.submitState.collect { resource ->
                when (resource) {
                    null -> {
                        // Initial state
                        binding.btnSubmit.isEnabled = true
                    }
                    is Resource.Loading -> {
                        binding.btnSubmit.isEnabled = false
                    }
                    is Resource.Success -> {
                        binding.btnSubmit.isEnabled = true
                        resource.data?.let { result ->
                            navigateToResults(testId)
                        }
                    }
                    is Resource.Error -> {
                        binding.btnSubmit.isEnabled = true
                        Toast.makeText(this@PracticeTestActivity, resource.message ?: "Error submitting test", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun displayQuestion() {
        if (currentQuestions.isEmpty()) return

        val question = currentQuestions[currentQuestionIndex]
        
        binding.tvQuestionNumber.text = "Question ${currentQuestionIndex + 1} of ${currentQuestions.size}"
        binding.tvQuestion.text = question.question
        binding.rbOption1.text = question.options["A"]
        binding.rbOption2.text = question.options["B"]
        binding.rbOption3.text = question.options["C"]
        binding.rbOption4.text = question.options["D"]

        // Explicitly uncheck all radio buttons first
        binding.rbOption1.isChecked = false
        binding.rbOption2.isChecked = false
        binding.rbOption3.isChecked = false
        binding.rbOption4.isChecked = false
        
        // Clear radio group
        binding.radioGroup.clearCheck()

        // Restore previous answer if exists
        val previousAnswer = userAnswers[currentQuestionIndex]
        android.util.Log.d("PracticeTest", "Displaying Q${currentQuestionIndex + 1}, Previous answer: $previousAnswer")
        android.util.Log.d("PracticeTest", "All answers: $userAnswers")
        
        when (previousAnswer) {
            "A" -> binding.rbOption1.isChecked = true
            "B" -> binding.rbOption2.isChecked = true
            "C" -> binding.rbOption3.isChecked = true
            "D" -> binding.rbOption4.isChecked = true
        }

        // Update button states
        binding.btnPrevious.isEnabled = currentQuestionIndex > 0
        binding.btnNext.visibility = if (currentQuestionIndex < currentQuestions.size - 1) View.VISIBLE else View.GONE
        binding.btnSubmit.visibility = if (currentQuestionIndex == currentQuestions.size - 1) View.VISIBLE else View.GONE
    }

    private fun submitTest() {
        if (userAnswers.size < currentQuestions.size) {
            Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert Map<Int, String> to List<Int> (option indices)
        val answersList = mutableListOf<Int>()
        for (i in currentQuestions.indices) {
            val answer = userAnswers[i] ?: "A"
            val answerIndex = when (answer) {
                "A" -> 0
                "B" -> 1
                "C" -> 2
                "D" -> 3
                else -> 0
            }
            answersList.add(answerIndex)
        }

        viewModel.submitTest(testId, answersList)
    }

    private fun navigateToResults(testId: String) {
        val intent = Intent(this, TestResultActivity::class.java)
        intent.putExtra("TEST_ID", testId)
        startActivity(intent)
        finish()
    }
}
