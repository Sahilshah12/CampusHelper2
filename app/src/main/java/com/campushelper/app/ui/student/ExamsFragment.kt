package com.campushelper.app.ui.student

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.campushelper.app.databinding.FragmentExamsBinding
import com.campushelper.app.ui.adapter.ExamAdapter
import com.campushelper.app.ui.activity.PracticeTestActivity
import com.campushelper.app.ui.viewmodel.CompetitiveExamViewModel
import com.campushelper.app.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExamsFragment : Fragment() {

    private var _binding: FragmentExamsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CompetitiveExamViewModel by viewModels()
    private lateinit var examAdapter: ExamAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeViewModel()
        
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadExams()
        }
    }

    private fun setupRecyclerView() {
        examAdapter = ExamAdapter { exam ->
            showTopicInputDialog(exam.name, exam._id)
        }
        
        binding.rvExams.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = examAdapter
        }
    }

    private fun showTopicInputDialog(examName: String, examId: String) {
        val input = EditText(requireContext()).apply {
            hint = "Enter topic (e.g., Physics - Mechanics, Chemistry - Organic)"
            setPadding(50, 40, 50, 40)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("$examName Practice Test")
            .setMessage("Which topic would you like to practice?")
            .setView(input)
            .setPositiveButton("Generate Test") { dialog, _ ->
                val topic = input.text.toString().trim()
                if (topic.isNotEmpty()) {
                    startPracticeTest(examId, topic, examName)
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Please enter a topic", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun startPracticeTest(examId: String, topic: String, examName: String) {
        val intent = Intent(requireContext(), PracticeTestActivity::class.java).apply {
            // Use "general" as subjectId for competitive exams since exam ID won't work
            putExtra("SUBJECT_ID", "general")
            putExtra("TOPIC", "$examName - $topic")
            putExtra("EXAM_NAME", examName)
        }
        startActivity(intent)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.examsState.collect { resource ->
                when (resource) {
                    null -> {
                        // Initial state
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                    }
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.swipeRefresh.isRefreshing = false
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        resource.data?.let { exams ->
                            examAdapter.submitList(exams)
                            binding.emptyView.visibility = if (exams.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
