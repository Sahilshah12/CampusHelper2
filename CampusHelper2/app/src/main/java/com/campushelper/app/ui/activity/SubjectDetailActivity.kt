package com.campushelper.app.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.campushelper.app.databinding.ActivitySubjectDetailBinding
import com.campushelper.app.data.remote.ApiService
import com.campushelper.app.ui.adapter.MaterialAdapter
import com.campushelper.app.ui.viewmodel.SubjectViewModel
import com.campushelper.app.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SubjectDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubjectDetailBinding
    private val viewModel: SubjectViewModel by viewModels()
    private lateinit var materialAdapter: MaterialAdapter
    private var subjectId: String = ""
    private var currentDialog: AlertDialog? = null
    
    @Inject
    lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subjectId = intent.getStringExtra("SUBJECT_ID") ?: ""
        val subjectName = intent.getStringExtra("SUBJECT_NAME") ?: "Subject"

        setupToolbar(subjectName)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        viewModel.getMaterials(subjectId)
    }

    private fun setupToolbar(title: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            this.title = title
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        materialAdapter = MaterialAdapter { material ->
            openMaterial(material)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SubjectDetailActivity)
            adapter = materialAdapter
        }
    }

    private fun openMaterial(material: com.campushelper.app.data.model.Material) {
        when (material.type.lowercase()) {
            "youtube" -> {
                material.url?.let { url ->
                    openYouTubeVideo(url)
                } ?: Toast.makeText(this, "No URL available", Toast.LENGTH_SHORT).show()
            }
            "pdf" -> {
                material.fileUrl?.let { fileUrl ->
                    openPdfFile(fileUrl)
                } ?: Toast.makeText(this, "No file available", Toast.LENGTH_SHORT).show()
            }
            "link" -> {
                material.url?.let { url ->
                    openWebLink(url)
                } ?: Toast.makeText(this, "No URL available", Toast.LENGTH_SHORT).show()
            }
            "notes" -> {
                material.content?.let { content ->
                    showNotesDialog(material.title, content)
                } ?: Toast.makeText(this, "No content available", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Unknown material type", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openYouTubeVideo(url: String) {
        try {
            // Try to open in YouTube app
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setPackage("com.google.android.youtube")
            startActivity(intent)
        } catch (e: Exception) {
            // If YouTube app not available, open in browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

    private fun openPdfFile(fileUrl: String) {
        val fullUrl = "https://campushelper-be.onrender.com$fileUrl"
        
        currentDialog?.dismiss()
        currentDialog = AlertDialog.Builder(this)
            .setTitle("Open PDF")
            .setMessage("How would you like to open this PDF?")
            .setPositiveButton("View in Browser") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                startActivity(intent)
            }
            .setNegativeButton("Download") { _, _ ->
                downloadPdf(fullUrl)
            }
            .setNeutralButton("Cancel", null)
            .create()
        currentDialog?.show()
    }

    private fun downloadPdf(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            Toast.makeText(this, "Opening PDF...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWebLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening link: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNotesDialog(title: String, content: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(content)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun setupClickListeners() {
        binding.fabAiChat.setOnClickListener {
            val intent = Intent(this, AiChatActivity::class.java)
            intent.putExtra("SUBJECT_ID", subjectId)
            startActivity(intent)
        }

        binding.btnPracticeTest.setOnClickListener {
            showSubjectSelectionDialog()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.materials.collect { resource ->
                when (resource) {
                    null -> {
                        // Initial state
                        binding.progressBar.visibility = View.GONE
                    }
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        resource.data?.let { materials ->
                            materialAdapter.submitList(materials)
                            binding.emptyView.visibility = if (materials.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@SubjectDetailActivity, resource.message ?: "Error loading materials", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showSubjectSelectionDialog() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = apiService.getSubjects()
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val subjects = response.body()!!.subjects
                    val subjectNames = subjects.map { it.name }.toTypedArray()

                    currentDialog?.dismiss()
                    currentDialog = AlertDialog.Builder(this@SubjectDetailActivity)
                        .setTitle("Select Subject for Practice Test")
                        .setItems(subjectNames) { _, which ->
                            val selectedSubject = subjects[which]
                            showTopicInputDialog(selectedSubject._id, selectedSubject.name)
                        }
                        .setNegativeButton("Cancel", null)
                        .create()
                    currentDialog?.show()
                } else {
                    Toast.makeText(this@SubjectDetailActivity, "Failed to load subjects", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@SubjectDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTopicInputDialog(selectedSubjectId: String, subjectName: String) {
        val input = EditText(this).apply {
            hint = "Enter topic (e.g., Sorting Algorithms)"
            setPadding(50, 30, 50, 30)
        }

        currentDialog?.dismiss()
        currentDialog = AlertDialog.Builder(this)
            .setTitle("Practice Test on $subjectName")
            .setMessage("Enter the topic you want to practice:")
            .setView(input)
            .setPositiveButton("Generate Test") { _, _ ->
                val topic = input.text.toString().trim()
                if (topic.isNotEmpty()) {
                    startPracticeTest(selectedSubjectId, topic)
                } else {
                    Toast.makeText(this, "Please enter a topic", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        currentDialog?.show()
    }

    override fun onDestroy() {
        currentDialog?.dismiss()
        super.onDestroy()
    }

    private fun startPracticeTest(selectedSubjectId: String, topic: String) {
        val intent = Intent(this, PracticeTestActivity::class.java).apply {
            putExtra("SUBJECT_ID", selectedSubjectId)
            putExtra("TOPIC", topic)
            putExtra("TEST_TYPE", "practice") // Mark as practice test
        }
        startActivity(intent)
    }
}
