package com.campushelper.app.ui.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.campushelper.app.databinding.ActivityAddMaterialBinding
import com.campushelper.app.ui.viewmodel.AdminViewModel
import com.campushelper.app.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class AddMaterialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMaterialBinding
    private val viewModel: AdminViewModel by viewModels()
    private var subjectId: String = ""
    private var subjectName: String = ""
    private var selectedFileUri: Uri? = null
    private var selectedFileName: String = ""

    private val pdfPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                selectedFileName = getFileName(uri)
                binding.tvSelectedFile.text = selectedFileName
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMaterialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subjectId = intent.getStringExtra("SUBJECT_ID") ?: ""
        subjectName = intent.getStringExtra("SUBJECT_NAME") ?: ""

        if (subjectId.isEmpty()) {
            Toast.makeText(this, "Invalid subject", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        setupListeners()
        observeViewModel()
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.tvSubjectName.text = subjectName
    }

    private fun setupListeners() {
        // Radio group listener
        binding.rgMaterialType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.rbPDF.id -> {
                    binding.layoutPDFUpload.visibility = View.VISIBLE
                    binding.layoutYouTubeLink.visibility = View.GONE
                }
                binding.rbVideo.id -> {
                    binding.layoutPDFUpload.visibility = View.GONE
                    binding.layoutYouTubeLink.visibility = View.VISIBLE
                }
            }
        }

        // PDF selection
        binding.btnSelectPDF.setOnClickListener {
            openPDFPicker()
        }

        // Upload button
        binding.btnUpload.setOnClickListener {
            uploadMaterial()
        }
    }

    private fun openPDFPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        pdfPickerLauncher.launch(intent)
    }

    private fun getFileName(uri: Uri): String {
        var fileName = ""
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    private fun uploadMaterial() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter material title", Toast.LENGTH_SHORT).show()
            return
        }

        val isPDF = binding.rbPDF.isChecked

        if (isPDF) {
            if (selectedFileUri == null) {
                Toast.makeText(this, "Please select a PDF file", Toast.LENGTH_SHORT).show()
                return
            }
            uploadPDFMaterial(title, description)
        } else {
            val youtubeUrl = binding.etYouTubeUrl.text.toString().trim()
            if (youtubeUrl.isEmpty()) {
                Toast.makeText(this, "Please enter YouTube URL", Toast.LENGTH_SHORT).show()
                return
            }
            uploadVideoMaterial(title, description, youtubeUrl)
        }
    }

    private fun uploadPDFMaterial(title: String, description: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnUpload.isEnabled = false

        lifecycleScope.launch {
            try {
                val file = createTempFileFromUri(selectedFileUri!!)
                if (file == null) {
                    Toast.makeText(this@AddMaterialActivity, "Failed to read file", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.btnUpload.isEnabled = true
                    return@launch
                }

                val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val subjectIdBody = subjectId.toRequestBody("text/plain".toMediaTypeOrNull())
                val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
                val typeBody = "pdf".toRequestBody("text/plain".toMediaTypeOrNull())

                viewModel.uploadMaterial(
                    subjectIdBody,
                    titleBody,
                    descriptionBody,
                    typeBody,
                    filePart
                )
            } catch (e: Exception) {
                Toast.makeText(this@AddMaterialActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.btnUpload.isEnabled = true
            }
        }
    }

    private fun uploadVideoMaterial(title: String, description: String, url: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnUpload.isEnabled = false

        lifecycleScope.launch {
            val subjectIdBody = subjectId.toRequestBody("text/plain".toMediaTypeOrNull())
            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val typeBody = "youtube".toRequestBody("text/plain".toMediaTypeOrNull())
            val urlBody = url.toRequestBody("text/plain".toMediaTypeOrNull())

            viewModel.uploadMaterial(
                subjectIdBody,
                titleBody,
                descriptionBody,
                typeBody,
                null,
                urlBody
            )
        }
    }

    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(cacheDir, selectedFileName)
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uploadMaterialState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnUpload.isEnabled = false
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@AddMaterialActivity,
                            "Material uploaded successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnUpload.isEnabled = true
                        Toast.makeText(
                            this@AddMaterialActivity,
                            resource.message ?: "Upload failed",
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
}
