package com.campushelper.app.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.campushelper.app.databinding.ActivityViewUsersBinding
import com.campushelper.app.data.remote.ApiService
import com.campushelper.app.ui.adapter.UserAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ViewUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewUsersBinding
    private lateinit var userAdapter: UserAdapter
    
    @Inject
    lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        setupRecyclerView()
        loadUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter()
        
        binding.rvUsers.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(this@ViewUsersActivity)
        }
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            try {
                binding.progressBar.isVisible = true
                binding.rvUsers.isVisible = false
                binding.layoutEmpty.isVisible = false
                
                val response = apiService.getAllUsers()
                
                if (response.isSuccessful && response.body() != null) {
                    val users = response.body()!!.users
                    
                    binding.progressBar.isVisible = false
                    
                    if (users.isEmpty()) {
                        binding.layoutEmpty.isVisible = true
                        binding.rvUsers.isVisible = false
                    } else {
                        binding.layoutEmpty.isVisible = false
                        binding.rvUsers.isVisible = true
                        userAdapter.submitList(users)
                    }
                } else {
                    binding.progressBar.isVisible = false
                    binding.layoutEmpty.isVisible = true
                    Toast.makeText(this@ViewUsersActivity, "Failed to load users", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.isVisible = false
                binding.layoutEmpty.isVisible = true
                Toast.makeText(this@ViewUsersActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
