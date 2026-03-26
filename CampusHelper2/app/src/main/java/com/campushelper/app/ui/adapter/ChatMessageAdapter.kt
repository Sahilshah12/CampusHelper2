package com.campushelper.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.campushelper.app.databinding.ItemChatMessageBinding
import com.campushelper.app.data.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

class ChatMessageAdapter : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun removeMessage(position: Int) {
        if (position >= 0 && position < messages.size) {
            messages.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun setMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    class MessageViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(message.timestamp))

            if (message.isUser) {
                // Show user message card, hide AI message card
                binding.userMessageCard.visibility = View.VISIBLE
                binding.aiMessageCard.visibility = View.GONE
                binding.tvUserMessage.text = message.text
                binding.tvUserTimestamp.text = formattedTime
            } else {
                // Show AI message card, hide user message card
                binding.aiMessageCard.visibility = View.VISIBLE
                binding.userMessageCard.visibility = View.GONE
                binding.tvAiMessage.text = message.text
                binding.tvAiTimestamp.text = formattedTime
            }
        }
    }
}
