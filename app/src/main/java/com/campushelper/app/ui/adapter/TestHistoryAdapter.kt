package com.campushelper.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.campushelper.app.data.model.Test
import com.campushelper.app.databinding.ItemTestHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class TestHistoryAdapter(
    private val onTestClick: (Test) -> Unit
) : RecyclerView.Adapter<TestHistoryAdapter.TestHistoryViewHolder>() {

    private val tests = mutableListOf<Test>()

    fun submitList(newTests: List<Test>) {
        tests.clear()
        tests.addAll(newTests)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestHistoryViewHolder {
        val binding = ItemTestHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TestHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TestHistoryViewHolder, position: Int) {
        holder.bind(tests[position])
    }

    override fun getItemCount() = tests.size

    inner class TestHistoryViewHolder(
        private val binding: ItemTestHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(test: Test) {
            binding.apply {
                tvTestTitle.text = test.title
                tvScore.text = "${test.score.toInt()}%"
                tvCorrectAnswers.text = "${test.correctAnswers}/${test.totalQuestions}"
                tvTotalQuestions.text = test.totalQuestions.toString()
                
                // Status chip
                chipStatus.text = when(test.status) {
                    "completed" -> "Completed"
                    "in_progress" -> "In Progress"
                    else -> "Pending"
                }
                
                // Format date (you might want to use a proper date parser)
                tvDate.text = formatDate(System.currentTimeMillis())
                
                btnViewDetails.setOnClickListener {
                    onTestClick(test)
                }
                
                root.setOnClickListener {
                    onTestClick(test)
                }
            }
        }
        
        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}
