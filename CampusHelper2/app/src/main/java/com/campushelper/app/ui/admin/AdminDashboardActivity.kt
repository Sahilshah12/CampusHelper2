package com.campushelper.app.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.campushelper.app.R
import com.campushelper.app.databinding.ActivityAdminDashboardBinding
import com.campushelper.app.ui.auth.LoginActivity
import com.campushelper.app.ui.viewmodel.ProgressViewModel
import com.campushelper.app.utils.Resource
import com.campushelper.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private var currentDialog: AlertDialog? = null
    
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        
        setupClickListeners()
        loadDashboardData()
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            showLogoutDialog()
        }
        
        binding.cardSubjects.setOnClickListener {
            startActivity(Intent(this, ManageSubjectsActivity::class.java))
        }
        
        binding.cardMaterials.setOnClickListener {
            startActivity(Intent(this, ManageMaterialsActivity::class.java))
        }
        
        binding.cardExams.setOnClickListener {
            startActivity(Intent(this, ManageExamsActivity::class.java))
        }
        
        binding.cardUsers.setOnClickListener {
            startActivity(Intent(this, ViewUsersActivity::class.java))
        }
    }

    private fun loadDashboardData() {
        // TODO: Load actual analytics data from backend
        binding.tvTotalStudents.text = "3"
        binding.tvTotalTests.text = "0"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.admin_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                loadDashboardData()
                Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        currentDialog?.dismiss()
        currentDialog = AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .create()
        currentDialog?.show()
    }

    private fun logout() {
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
        sessionManager.clearSession()
        
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finishAffinity() // Finish all activities in the task
    }

    override fun onDestroy() {
        currentDialog?.dismiss()
        super.onDestroy()
    }
}
