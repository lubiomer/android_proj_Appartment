package com.management.roomates.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.management.roomates.data.model.GroceryItem
import com.management.roomates.databinding.ItemPurchaseBinding

class PurchasesAdapter : ListAdapter<GroceryItem, PurchasesAdapter.PurchaseViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val binding = ItemPurchaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PurchaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PurchaseViewHolder(private val binding: ItemPurchaseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(groceryItem: GroceryItem) {
            binding.groceryItem = groceryItem
            binding.executePendingBindings()
        }
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
