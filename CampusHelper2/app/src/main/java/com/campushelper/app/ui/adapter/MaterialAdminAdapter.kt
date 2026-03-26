package com.campushelper.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.campushelper.app.data.model.Material
import com.campushelper.app.databinding.ItemMaterialAdminBinding

class MaterialAdminAdapter(
    private val onEditClick: (Material) -> Unit,
    private val onDeleteClick: (Material) -> Unit
) : ListAdapter<Material, MaterialAdminAdapter.MaterialViewHolder>(MaterialDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val binding = ItemMaterialAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MaterialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MaterialViewHolder(
        private val binding: ItemMaterialAdminBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(material: Material) {
            binding.apply {
                tvMaterialTitle.text = material.title
                tvMaterialType.text = material.type.uppercase()
                tvMaterialDescription.text = material.description
                
                btnEdit.setOnClickListener {
                    onEditClick(material)
                }
                
                btnDelete.setOnClickListener {
                    onDeleteClick(material)
                }
            }
        }
    }

    private class MaterialDiffCallback : DiffUtil.ItemCallback<Material>() {
        override fun areItemsTheSame(oldItem: Material, newItem: Material): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Material, newItem: Material): Boolean {
            return oldItem == newItem
        }
    }
}
