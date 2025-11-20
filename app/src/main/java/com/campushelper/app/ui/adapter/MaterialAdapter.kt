package com.campushelper.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.campushelper.app.databinding.ItemMaterialBinding
import com.campushelper.app.data.model.Material

class MaterialAdapter(
    private val onItemClick: (Material) -> Unit
) : ListAdapter<Material, MaterialAdapter.MaterialViewHolder>(MaterialDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val binding = ItemMaterialBinding.inflate(
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
        private val binding: ItemMaterialBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(material: Material) {
            binding.tvMaterialTitle.text = material.title
            binding.tvMaterialDescription.text = material.description ?: "No description"
            binding.tvMaterialType.text = material.type.uppercase()
            
            // Set icon based on material type
            val iconRes = when (material.type.lowercase()) {
                "pdf" -> android.R.drawable.ic_menu_save
                "youtube" -> android.R.drawable.ic_menu_view
                "notes" -> android.R.drawable.ic_menu_edit
                "link" -> android.R.drawable.ic_menu_share
                else -> android.R.drawable.ic_menu_info_details
            }
            binding.ivMaterialIcon.setImageResource(iconRes)
            
            binding.root.setOnClickListener {
                onItemClick(material)
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
