package com.campushelper.app.ui.student

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.campushelper.app.databinding.FragmentHomeBinding
import com.campushelper.app.ui.activity.SubjectDetailActivity
import com.campushelper.app.ui.adapters.SubjectAdapter
import com.campushelper.app.ui.viewmodel.SubjectViewModel
import com.campushelper.app.utils.Resource
import com.campushelper.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SubjectViewModel by viewModels()
    private lateinit var adapter: SubjectAdapter
    
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupRecyclerView()
        observeViewModel()
        
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadSubjects()
        }
    }

    private fun setupUI() {
        // Set greeting based on time
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val greeting = when (hourOfDay) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
        binding.tvGreeting.text = greeting

        // Get user name from SessionManager
        val userName = sessionManager.getUserName() ?: "Student"
        binding.tvUserName.text = userName

        // Set date
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(Date())
    }

    private fun setupRecyclerView() {
        adapter = SubjectAdapter { subject ->
            val intent = Intent(requireContext(), SubjectDetailActivity::class.java)
            intent.putExtra("SUBJECT_ID", subject._id)
            intent.putExtra("SUBJECT_NAME", subject.name)
            startActivity(intent)
        }
        
        binding.rvSubjects.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSubjects.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.subjectsState.collect { resource ->
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
                            resource.data?.let { 
                                adapter.submitList(it)
                                // Update statistics
                                binding.tvSubjectsCount.text = it.size.toString()
                                // TODO: Calculate actual progress from backend
                                binding.tvProgressPercent.text = "75%"
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
