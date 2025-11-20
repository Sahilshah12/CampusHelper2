package com.campushelper.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.campushelper.app.data.model.Subject
import com.campushelper.app.databinding.ItemSubjectAdminBinding

class SubjectAdminAdapter(
    private val onAddMaterialClick: (Subject) -> Unit,
    private val onEditClick: (Subject) -> Unit,
    private val onDeleteClick: (Subject) -> Unit
) : ListAdapter<Subject, SubjectAdminAdapter.SubjectViewHolder>(SubjectDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubjectViewHolder(
        private val binding: ItemSubjectAdminBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subject: Subject) {
            binding.apply {
                tvSubjectName.text = subject.name
                tvSubjectCode.text = subject.code
                tvSubjectDescription.text = subject.description
                
                btnAddMaterial.setOnClickListener {
                    onAddMaterialClick(subject)
                }
                
                btnEdit.setOnClickListener {
                    onEditClick(subject)
                }
                
                btnDelete.setOnClickListener {
                    onDeleteClick(subject)
                }
            }
        }
    }

    private class SubjectDiffCallback : DiffUtil.ItemCallback<Subject>() {
        override fun areItemsTheSame(oldItem: Subject, newItem: Subject): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: Subject, newItem: Subject): Boolean {
            return oldItem == newItem
        }
    }
}
