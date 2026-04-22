package com.campushelper.app.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.campushelper.app.data.model.User
import com.campushelper.app.databinding.ActivityViewUsersBinding
import com.campushelper.app.ui.adapter.UserAdapter
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ViewUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewUsersBinding
    private lateinit var userAdapter: UserAdapter
    private val usersRef = FirebaseDatabase.getInstance().reference.child("users")

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

                val snapshot = usersRef.get().await()
                val users = snapshot.children.mapNotNull { child ->
                    val id = child.child("id").getValue(String::class.java)
                        ?: child.key
                        ?: return@mapNotNull null
                    val name = child.child("name").getValue(String::class.java) ?: return@mapNotNull null
                    val email = child.child("email").getValue(String::class.java) ?: return@mapNotNull null
                    val role = child.child("role").getValue(String::class.java) ?: "student"
                    User(id = id, name = name, email = email, role = role)
                }

                binding.progressBar.isVisible = false

                if (users.isEmpty()) {
                    binding.layoutEmpty.isVisible = true
                    binding.rvUsers.isVisible = false
                } else {
                    binding.layoutEmpty.isVisible = false
                    binding.rvUsers.isVisible = true
                    userAdapter.submitList(users)
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
