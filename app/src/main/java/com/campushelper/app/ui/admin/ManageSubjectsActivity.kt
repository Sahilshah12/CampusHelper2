package com.campushelper.app.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.campushelper.app.R
import com.campushelper.app.databinding.ActivityManageSubjectsBinding
import com.campushelper.app.data.model.CreateSubjectRequest
import com.campushelper.app.data.model.Subject
import com.campushelper.app.ui.adapter.SubjectAdminAdapter
import com.campushelper.app.ui.viewmodel.AdminViewModel
import com.campushelper.app.ui.viewmodel.SubjectViewModel
import com.campushelper.app.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageSubjectsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageSubjectsBinding
    private val subjectViewModel: SubjectViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()
    private lateinit var subjectAdapter: SubjectAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageSubjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        setupRecyclerView()
        observeSubjects()
        observeAdminOperations()
        
        binding.fabAddSubject.setOnClickListener {
            showAddSubjectDialog()
        }
        
        // Load subjects
        subjectViewModel.loadSubjects()
    }

    private fun setupRecyclerView() {
        subjectAdapter = SubjectAdminAdapter(
            onAddMaterialClick = { subject ->
                openAddMaterialActivity(subject)
            },
            onEditClick = { subject ->
                showEditSubjectDialog(subject)
            },
            onDeleteClick = { subject ->
                showDeleteConfirmation(subject)
            }
        )
        
        binding.rvSubjects.apply {
            adapter = subjectAdapter
            layoutManager = LinearLayoutManager(this@ManageSubjectsActivity)
        }
    }

    private fun openAddMaterialActivity(subject: Subject) {
        val intent = Intent(this, AddMaterialActivity::class.java).apply {
            putExtra("SUBJECT_ID", subject._id)
            putExtra("SUBJECT_NAME", subject.name)
        }
        startActivity(intent)
    }

    private fun observeSubjects() {
        lifecycleScope.launch {
            subjectViewModel.subjectsState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.rvSubjects.isVisible = false
                        binding.layoutEmpty.isVisible = false
                    }
                    is Resource.Success -> {
                        binding.progressBar.isVisible = false
                        val subjects = resource.data
                        if (subjects.isNullOrEmpty()) {
                            binding.layoutEmpty.isVisible = true
                            binding.rvSubjects.isVisible = false
                        } else {
                            binding.layoutEmpty.isVisible = false
                            binding.rvSubjects.isVisible = true
                            subjectAdapter.submitList(subjects)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.isVisible = false
                        binding.layoutEmpty.isVisible = true
                        binding.rvSubjects.isVisible = false
                        Toast.makeText(this@ManageSubjectsActivity, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    null -> {
                        binding.progressBar.isVisible = false
                    }
                }
            }
        }
    }

    private fun observeAdminOperations() {
        // Observe create subject
        lifecycleScope.launch {
            adminViewModel.createSubjectState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading
                    }
                    is Resource.Success -> {
                        Toast.makeText(this@ManageSubjectsActivity, "Subject created successfully", Toast.LENGTH_SHORT).show()
                        adminViewModel.resetCreateSubjectState()
                        subjectViewModel.loadSubjects() // Refresh list
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@ManageSubjectsActivity, resource.message, Toast.LENGTH_SHORT).show()
                        adminViewModel.resetCreateSubjectState()
                    }
                    null -> {}
                }
            }
        }

        // Observe delete subject
        lifecycleScope.launch {
            adminViewModel.deleteSubjectState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading
                    }
                    is Resource.Success -> {
                        Toast.makeText(this@ManageSubjectsActivity, "Subject deleted successfully", Toast.LENGTH_SHORT).show()
                        adminViewModel.resetDeleteSubjectState()
                        subjectViewModel.loadSubjects() // Refresh list
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@ManageSubjectsActivity, resource.message, Toast.LENGTH_SHORT).show()
                        adminViewModel.resetDeleteSubjectState()
                    }
                    null -> {}
                }
            }
        }
    }

    private fun showAddSubjectDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_subject, null)
        val etName = dialogView.findViewById<EditText>(R.id.etSubjectName)
        val etCode = dialogView.findViewById<EditText>(R.id.etSubjectCode)
        val etDescription = dialogView.findViewById<EditText>(R.id.etSubjectDescription)
        
        AlertDialog.Builder(this)
            .setTitle("Add New Subject")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                val code = etCode.text.toString().trim()
                val description = etDescription.text.toString().trim()
                
                if (name.isNotEmpty() && code.isNotEmpty()) {
                    val request = CreateSubjectRequest(
                        name = name,
                        code = code,
                        description = description
                    )
                    adminViewModel.createSubject(request)
                } else {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditSubjectDialog(subject: Subject) {
        Toast.makeText(this, "Edit feature coming soon", Toast.LENGTH_SHORT).show()
        // TODO: Implement edit dialog
    }

    private fun showDeleteConfirmation(subject: Subject) {
        AlertDialog.Builder(this)
            .setTitle("Delete Subject")
            .setMessage("Are you sure you want to delete ${subject.name}?")
            .setPositiveButton("Delete") { _, _ ->
                adminViewModel.deleteSubject(subject._id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
