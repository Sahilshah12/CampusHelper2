package com.campushelper.app.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.campushelper.app.databinding.ActivityAiChatBinding
import com.campushelper.app.data.model.ChatMessage
import com.campushelper.app.ui.adapter.ChatMessageAdapter
import com.campushelper.app.ui.viewmodel.AiChatViewModel
import com.campushelper.app.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AiChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiChatBinding
    private val viewModel: AiChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatMessageAdapter
    private var subjectId: String = ""
    private var thinkingMessagePosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subjectId = intent.getStringExtra("SUBJECT_ID") ?: "general"

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "AI Assistant"
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatMessageAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AiChatActivity)
            adapter = chatAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.etMessage.text?.clear()
            }
        }
    }

    private fun sendMessage(message: String) {
        // Add user message to chat
        val userMessage = ChatMessage(
            text = message,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )
        chatAdapter.addMessage(userMessage)
        
        // Add "thinking" placeholder message
        val thinkingMessage = ChatMessage(
            text = "Thinking...",
            isUser = false,
            timestamp = System.currentTimeMillis()
        )
        chatAdapter.addMessage(thinkingMessage)
        thinkingMessagePosition = chatAdapter.itemCount - 1
        binding.recyclerView.scrollToPosition(thinkingMessagePosition)

        // Send to AI - use "General" as topic if no specific topic
        val topic = intent.getStringExtra("TOPIC") ?: "General Discussion"
        viewModel.sendMessage(subjectId, topic, message)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.chatState.collect { resource ->
                when (resource) {
                    null -> {
                        // Initial state - do nothing
                        binding.progressBar.visibility = View.GONE
                        binding.btnSend.isEnabled = true
                    }
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSend.isEnabled = false
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSend.isEnabled = true
                        
                        // Remove "thinking" placeholder
                        if (thinkingMessagePosition >= 0) {
                            chatAdapter.removeMessage(thinkingMessagePosition)
                            thinkingMessagePosition = -1
                        }
                        
                        resource.data?.let { responseText ->
                            if (responseText.isNotBlank()) {
                                val aiMessage = ChatMessage(
                                    text = responseText,
                                    isUser = false,
                                    timestamp = System.currentTimeMillis()
                                )
                                chatAdapter.addMessage(aiMessage)
                                binding.recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                            } else {
                                Toast.makeText(
                                    this@AiChatActivity,
                                    "AI returned an empty response. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSend.isEnabled = true
                        
                        // Remove "thinking" placeholder
                        if (thinkingMessagePosition >= 0) {
                            chatAdapter.removeMessage(thinkingMessagePosition)
                            thinkingMessagePosition = -1
                        }
                        
                        // Add error message to chat
                        val errorMessage = ChatMessage(
                            text = "Error: ${resource.message ?: "Failed to get response"}",
                            isUser = false,
                            timestamp = System.currentTimeMillis()
                        )
                        chatAdapter.addMessage(errorMessage)
                        binding.recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                        
                        Toast.makeText(this@AiChatActivity, resource.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
