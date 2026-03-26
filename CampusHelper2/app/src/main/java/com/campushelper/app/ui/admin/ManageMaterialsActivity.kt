package com.campushelper.app.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.campushelper.app.R
import com.campushelper.app.databinding.ActivityManageMaterialsBinding
import com.campushelper.app.data.model.CreateMaterialRequest
import com.campushelper.app.data.model.Material
import com.campushelper.app.data.model.Subject
import com.campushelper.app.ui.adapter.MaterialAdminAdapter
import com.campushelper.app.ui.viewmodel.AdminViewModel
import com.campushelper.app.ui.viewmodel.MaterialViewModel
import com.campushelper.app.ui.viewmodel.SubjectViewModel
import com.campushelper.app.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageMaterialsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageMaterialsBinding
    private val materialViewModel: MaterialViewModel by viewModels()
    private val subjectViewModel: SubjectViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()
    private lateinit var materialAdapter: MaterialAdminAdapter
    private var subjects: List<Subject> = emptyList()
    private var currentDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageMaterialsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        setupRecyclerView()
        observeMaterials()
        observeSubjects()
        observeAdminOperations()
        
        binding.fabAddMaterial.setOnClickListener {
            showAddMaterialDialog()
        }
        
        // Load data
        subjectViewModel.loadSubjects()
        materialViewModel.loadMaterials()
    }

    private fun setupRecyclerView() {
        materialAdapter = MaterialAdminAdapter(
            onEditClick = { material ->
                showEditMaterialDialog(material)
            },
            onDeleteClick = { material ->
                showDeleteConfirmation(material)
            }
        )
        
        binding.rvMaterials.apply {
            adapter = materialAdapter
            layoutManager = LinearLayoutManager(this@ManageMaterialsActivity)
        }
    }

    private fun observeMaterials() {
        lifecycleScope.launch {
            materialViewModel.materialsState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.rvMaterials.isVisible = false
                        binding.layoutEmpty.isVisible = false
                    }
                    is Resource.Success -> {
                        binding.progressBar.isVisible = false
                        val materials = resource.data
                        if (materials.isNullOrEmpty()) {
                            binding.layoutEmpty.isVisible = true
                            binding.rvMaterials.isVisible = false
                        } else {
                            binding.layoutEmpty.isVisible = false
                            binding.rvMaterials.isVisible = true
                            materialAdapter.submitList(materials)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.isVisible = false
                        binding.layoutEmpty.isVisible = true
                        binding.rvMaterials.isVisible = false
                        Toast.makeText(this@ManageMaterialsActivity, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    null -> {
                        binding.progressBar.isVisible = false
                    }
                }
            }
        }
    }

    private fun observeSubjects() {
        lifecycleScope.launch {
            subjectViewModel.subjectsState.collect { resource ->
                if (resource is Resource.Success) {
                    subjects = resource.data ?: emptyList()
                }
            }
        }
    }

    private fun observeAdminOperations() {
        // Observe create material
        lifecycleScope.launch {
            adminViewModel.createMaterialState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading
                    }
                    is Resource.Success -> {
                        Toast.makeText(this@ManageMaterialsActivity, "Material created successfully", Toast.LENGTH_SHORT).show()
                        adminViewModel.resetCreateMaterialState()
                        materialViewModel.loadMaterials() // Refresh list
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@ManageMaterialsActivity, resource.message, Toast.LENGTH_SHORT).show()
                        adminViewModel.resetCreateMaterialState()
                    }
                    null -> {}
                }
            }
        }

        // Observe delete material
        lifecycleScope.launch {
            adminViewModel.deleteMaterialState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading
                    }
                    is Resource.Success -> {
                        Toast.makeText(this@ManageMaterialsActivity, "Material deleted successfully", Toast.LENGTH_SHORT).show()
                        adminViewModel.resetDeleteMaterialState()
                        materialViewModel.loadMaterials() // Refresh list
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@ManageMaterialsActivity, resource.message, Toast.LENGTH_SHORT).show()
                        adminViewModel.resetDeleteMaterialState()
                    }
                    null -> {}
                }
            }
        }
    }

    private fun showAddMaterialDialog() {
        // Check if subjects are loaded
        if (subjects.isEmpty()) {
            Toast.makeText(this, "Loading subjects, please wait...", Toast.LENGTH_SHORT).show()
            return
        }
        
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_material, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etMaterialTitle)
        val actvType = dialogView.findViewById<AutoCompleteTextView>(R.id.actvMaterialType)
        val actvSubject = dialogView.findViewById<AutoCompleteTextView>(R.id.actvSubject)
        val etUrl = dialogView.findViewById<EditText>(R.id.etMaterialUrl)
        val etDescription = dialogView.findViewById<EditText>(R.id.etMaterialDescription)
        
        // Setup type dropdown
        val types = arrayOf("pdf", "video", "link")
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        actvType.setAdapter(typeAdapter)
        
        // Setup subject dropdown
        val subjectNames = subjects.map { it.name }.toTypedArray()
        val subjectAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, subjectNames)
        actvSubject.setAdapter(subjectAdapter)
        
        currentDialog?.dismiss()
        currentDialog = AlertDialog.Builder(this)
            .setTitle("Add New Material")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString().trim()
                val type = actvType.text.toString().trim()
                val subjectName = actvSubject.text.toString().trim()
                val url = etUrl.text.toString().trim()
                val description = etDescription.text.toString().trim()
                
                if (title.isNotEmpty() && type.isNotEmpty() && subjectName.isNotEmpty() && url.isNotEmpty()) {
                    val selectedSubject = subjects.find { it.name == subjectName }
                    if (selectedSubject != null) {
                        val request = CreateMaterialRequest(
                            subjectId = selectedSubject._id,
                            title = title,
                            type = type,
                            description = description,
                            url = url
                        )
                        adminViewModel.createMaterial(request)
                    } else {
                        Toast.makeText(this, "Please select a valid subject", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        currentDialog?.show()
    }

    private fun showEditMaterialDialog(material: Material) {
        Toast.makeText(this, "Edit feature coming soon", Toast.LENGTH_SHORT).show()
        // TODO: Implement edit dialog
    }

    private fun showDeleteConfirmation(material: Material) {
        currentDialog?.dismiss()
        currentDialog = AlertDialog.Builder(this)
            .setTitle("Delete Material")
            .setMessage("Are you sure you want to delete ${material.title}?")
            .setPositiveButton("Delete") { _, _ ->
                adminViewModel.deleteMaterial(material.id)
            }
            .setNegativeButton("Cancel", null)
            .create()
        currentDialog?.show()
    }

    override fun onDestroy() {
        currentDialog?.dismiss()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
