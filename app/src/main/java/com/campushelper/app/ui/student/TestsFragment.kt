package com.campushelper.app.ui.student

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.campushelper.app.databinding.FragmentTestsBinding
import com.campushelper.app.ui.adapter.TestHistoryAdapter
import com.campushelper.app.ui.activity.TestAnalysisActivity
import com.campushelper.app.ui.viewmodel.TestViewModel
import com.campushelper.app.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestsFragment : Fragment() {

    private var _binding: FragmentTestsBinding? = null
    private val binding get() = _binding!!
    
    private val testViewModel: TestViewModel by viewModels()
    private lateinit var testHistoryAdapter: TestHistoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeTests()
        setupSwipeRefresh()
        
        // Load tests
        loadTests()
    }
    
    private fun setupRecyclerView() {
        testHistoryAdapter = TestHistoryAdapter { test ->
            // Navigate to detailed test analysis
            if (test.status == "completed") {
                val intent = Intent(requireContext(), TestAnalysisActivity::class.java).apply {
                    putExtra("TEST_ID", test._id)
                }
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Test not completed yet", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.rvTests.apply {
            adapter = testHistoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadTests()
        }
    }
    
    private fun loadTests() {
        testViewModel.getTests(status = "completed")
    }
    
    private fun observeTests() {
        viewLifecycleOwner.lifecycleScope.launch {
            testViewModel.testsState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.swipeRefresh.isRefreshing = false
                    }
                    is Resource.Success -> {
                        binding.progressBar.isVisible = false
                        binding.swipeRefresh.isRefreshing = false
                        
                        val tests = resource.data
                        if (tests.isNullOrEmpty()) {
                            binding.layoutEmpty.isVisible = true
                            binding.rvTests.isVisible = false
                        } else {
                            binding.layoutEmpty.isVisible = false
                            binding.rvTests.isVisible = true
                            testHistoryAdapter.submitList(tests)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.isVisible = false
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    }
                    null -> {
                        binding.progressBar.isVisible = false
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
