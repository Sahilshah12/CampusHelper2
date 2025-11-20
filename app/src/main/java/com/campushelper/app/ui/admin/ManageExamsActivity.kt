package com.campushelper.app.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.campushelper.app.R
import com.campushelper.app.databinding.ActivityManageExamsBinding
import com.campushelper.app.data.model.CompetitiveExam
import com.campushelper.app.data.model.CreateCompetitiveExamRequest
import com.campushelper.app.ui.adapter.ExamAdminAdapter
import com.campushelper.app.ui.viewmodel.AdminViewModel
import com.campushelper.app.ui.viewmodel.CompetitiveExamViewModel
import com.campushelper.app.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageExamsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageExamsBinding
    private val examViewModel: CompetitiveExamViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()
    private lateinit var examAdapter: ExamAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageExamsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        setupRecyclerView()
        observeExams()
        observeAdminOperations()
        
        binding.fabAddExam.setOnClickListener {
            showAddExamDialog()
        }
        
        // Load exams
        examViewModel.loadExams()
    }

    private fun setupRecyclerView() {
        examAdapter = ExamAdminAdapter(
            onEditClick = { exam ->
                showEditExamDialog(exam)
            },
            onDeleteClick = { exam ->
                showDeleteConfirmation(exam)
            }
        )
        
        binding.rvExams.apply {
            adapter = examAdapter
            layoutManager = LinearLayoutManager(this@ManageExamsActivity)
        }
    }

    private fun observeExams() {
        lifecycleScope.launch {
            examViewModel.examsState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.rvExams.isVisible = false
                        binding.layoutEmpty.isVisible = false
                    }
                    is Resource.Success -> {
                        binding.progressBar.isVisible = false
                        val exams = resource.data
                        if (exams.isNullOrEmpty()) {
                            binding.layoutEmpty.isVisible = true
                            binding.rvExams.isVisible = false
                        } else {
                            binding.layoutEmpty.isVisible = false
                            binding.rvExams.isVisible = true
                            examAdapter.submitList(exams)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.isVisible = false
                        binding.layoutEmpty.isVisible = true
                        binding.rvExams.isVisible = false
                        Toast.makeText(this@ManageExamsActivity, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    null -> {
                        binding.progressBar.isVisible = false
                    }
                }
            }
        }
    }

    private fun observeAdminOperations() {
        // Observe create exam
        lifecycleScope.launch {
            adminViewModel.createExamState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading
                    }
                    is Resource.Success -> {
                        Toast.makeText(this@ManageExamsActivity, "Exam created successfully", Toast.LENGTH_SHORT).show()
                        adminViewModel.resetCreateExamState()
                        examViewModel.loadExams() // Refresh list
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@ManageExamsActivity, resource.message, Toast.LENGTH_SHORT).show()
                        adminViewModel.resetCreateExamState()
                    }
                    null -> {}
                }
            }
        }

        // Observe delete exam
        lifecycleScope.launch {
            adminViewModel.deleteExamState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading
                    }
                    is Resource.Success -> {
                        Toast.makeText(this@ManageExamsActivity, "Exam deleted successfully", Toast.LENGTH_SHORT).show()
                        adminViewModel.resetDeleteExamState()
                        examViewModel.loadExams() // Refresh list
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@ManageExamsActivity, resource.message, Toast.LENGTH_SHORT).show()
                        adminViewModel.resetDeleteExamState()
                    }
                    null -> {}
                }
            }
        }
    }

    private fun showAddExamDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_exam, null)
        val etName = dialogView.findViewById<EditText>(R.id.etExamName)
        val etShortName = dialogView.findViewById<EditText>(R.id.etExamShortName)
        val etDescription = dialogView.findViewById<EditText>(R.id.etExamDescription)
        val etTotalMarks = dialogView.findViewById<EditText>(R.id.etTotalMarks)
        val etDuration = dialogView.findViewById<EditText>(R.id.etDuration)
        val cbNegativeMarking = dialogView.findViewById<CheckBox>(R.id.cbNegativeMarking)
        val etEligibility = dialogView.findViewById<EditText>(R.id.etEligibility)
        
        AlertDialog.Builder(this)
            .setTitle("Add New Competitive Exam")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                val shortName = etShortName.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val totalMarks = etTotalMarks.text.toString().toIntOrNull() ?: 0
                val duration = etDuration.text.toString().toIntOrNull() ?: 0
                val negativeMarking = cbNegativeMarking.isChecked
                val eligibility = etEligibility.text.toString().trim()
                
                if (name.isNotEmpty() && shortName.isNotEmpty() && description.isNotEmpty() && totalMarks > 0 && duration > 0) {
                    val request = CreateCompetitiveExamRequest(
                        name = name,
                        shortName = shortName,
                        description = description,
                        subjects = emptyList(), // Empty for now
                        totalMarks = totalMarks,
                        duration = duration,
                        negativeMarking = negativeMarking,
                        eligibility = eligibility.ifEmpty { "As per official notification" }
                    )
                    adminViewModel.createCompetitiveExam(request)
                } else {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditExamDialog(exam: CompetitiveExam) {
        Toast.makeText(this, "Edit feature coming soon", Toast.LENGTH_SHORT).show()
        // TODO: Implement edit dialog
    }

    private fun showDeleteConfirmation(exam: CompetitiveExam) {
        AlertDialog.Builder(this)
            .setTitle("Delete Exam")
            .setMessage("Are you sure you want to delete ${exam.name}?")
            .setPositiveButton("Delete") { _, _ ->
                adminViewModel.deleteCompetitiveExam(exam._id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
