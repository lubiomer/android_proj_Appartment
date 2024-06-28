package com.management.roomates.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.management.roomates.R
import com.management.roomates.data.model.GroceryItem
import com.management.roomates.databinding.ItemGroceryBinding
import com.management.roomates.viewmodel.GroceriesViewModel

class GroceriesAdapter(
    private val groceriesViewModel: GroceriesViewModel,
    private val onEditClick: (GroceryItem) -> Unit
) : ListAdapter<GroceryItem, GroceriesAdapter.GroceryViewHolder>(DiffCallback()) {

    private var editingGroceryItemId: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryViewHolder {
        val binding = ItemGroceryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroceryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroceryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GroceryViewHolder(private val binding: ItemGroceryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val item = getItem(adapterPosition)
                if (editingGroceryItemId == item.id) {
                    setEditingGroceryItemId(null)
                    onEditClick(GroceryItem())
                } else {
                    setEditingGroceryItemId(item.id)
                    onEditClick(item)
                }
            }
            binding.buttonDelete.setOnClickListener {
                val item = getItem(adapterPosition)
                groceriesViewModel.delete(item)
            }
        }

        fun bind(groceryItem: GroceryItem) {
            binding.item = groceryItem
            if (editingGroceryItemId == groceryItem.id) {
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.editing_background))
            } else {
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.transparent))
            }
            binding.executePendingBindings()
        }
    }

    fun setEditingGroceryItemId(id: Int?) {
        editingGroceryItemId = id
        notifyDataSetChanged()
    }

    class DiffCallback : DiffUtil.ItemCallback<GroceryItem>() {
        override fun areItemsTheSame(oldItem: GroceryItem, newItem: GroceryItem): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: GroceryItem, newItem: GroceryItem): Boolean {
            return oldItem == newItem
        }
    }
}
