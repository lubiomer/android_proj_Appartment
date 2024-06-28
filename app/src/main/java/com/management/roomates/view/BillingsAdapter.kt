package com.management.roomates.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.management.roomates.R
import com.management.roomates.data.model.BillingItem
import com.management.roomates.databinding.ItemBillingBinding
import com.management.roomates.viewmodel.BillingsViewModel

class BillingsAdapter(
    private val billingsViewModel: BillingsViewModel,
    private val onItemClick: (BillingItem) -> Unit
) : ListAdapter<BillingItem, BillingsAdapter.BillingViewHolder>(DiffCallback()) {

    private var editingBillingItemId: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingViewHolder {
        val binding = ItemBillingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillingViewHolder(binding, billingsViewModel, onItemClick, editingBillingItemId)
    }

    override fun onBindViewHolder(holder: BillingViewHolder, position: Int) {
        holder.bind(getItem(position), editingBillingItemId)
    }

    fun setEditingBillingItemId(id: Int?) {
        editingBillingItemId = id
        notifyDataSetChanged()
    }

    class BillingViewHolder(
        private val binding: ItemBillingBinding,
        private val billingsViewModel: BillingsViewModel,
        private val onItemClick: (BillingItem) -> Unit,
        private var editingBillingItemId: Int?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(billingItem: BillingItem, editingBillingItemId: Int?) {
            binding.item = billingItem
            binding.executePendingBindings()

            if (billingItem.id == editingBillingItemId) {
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.editing_background))
            } else {
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.transparent))
            }

            binding.root.setOnClickListener {
                onItemClick(billingItem)
            }

            binding.buttonDelete.setOnClickListener {
                billingsViewModel.delete(billingItem)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<BillingItem>() {
        override fun areItemsTheSame(oldItem: BillingItem, newItem: BillingItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BillingItem, newItem: BillingItem): Boolean {
            return oldItem == newItem
        }
    }
}
