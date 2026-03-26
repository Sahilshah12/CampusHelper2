package com.campushelper.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.campushelper.app.data.model.CompetitiveExam
import com.campushelper.app.databinding.ItemExamAdminBinding

class ExamAdminAdapter(
    private val onEditClick: (CompetitiveExam) -> Unit,
    private val onDeleteClick: (CompetitiveExam) -> Unit
) : ListAdapter<CompetitiveExam, ExamAdminAdapter.ExamViewHolder>(ExamDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val binding = ItemExamAdminBinding.inflate(
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
        private val binding: ItemExamAdminBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exam: CompetitiveExam) {
            binding.apply {
                tvExamName.text = exam.name
                tvExamShortName.text = exam.shortName
                tvExamDescription.text = exam.description ?: "No description"
                
                // Show category and subject count
                tvExamDate.text = "Category: ${exam.category}"
                tvExamDetails.text = "Subjects: ${exam.subjects.size}"
                
                btnEdit.setOnClickListener {
                    onEditClick(exam)
                }
                
                btnDelete.setOnClickListener {
                    onDeleteClick(exam)
                }
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
