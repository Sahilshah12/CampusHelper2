package com.campushelper.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.campushelper.app.databinding.ItemExamBinding
import com.campushelper.app.data.model.CompetitiveExam

class ExamAdapter(
    private val onItemClick: (CompetitiveExam) -> Unit
) : ListAdapter<CompetitiveExam, ExamAdapter.ExamViewHolder>(ExamDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val binding = ItemExamBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExamViewHolder(
        private val binding: ItemExamBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exam: CompetitiveExam) {
            binding.tvExamName.text = exam.name
            binding.tvExamDescription.text = exam.description ?: ""
            binding.tvDailyTests.text = "Subjects: ${exam.subjects.size}"
            
            binding.root.setOnClickListener {
                onItemClick(exam)
            }
        }
    }

    private class ExamDiffCallback : DiffUtil.ItemCallback<CompetitiveExam>() {
        override fun areItemsTheSame(oldItem: CompetitiveExam, newItem: CompetitiveExam): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: CompetitiveExam, newItem: CompetitiveExam): Boolean {
            return oldItem == newItem
        }
    }
}
