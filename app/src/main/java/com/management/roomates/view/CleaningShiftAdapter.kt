package com.management.roomates.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.management.roomates.R
import com.management.roomates.data.model.CleaningShift
import com.management.roomates.databinding.ItemCleaningShiftBinding
import com.management.roomates.viewmodel.CleaningShiftViewModel

class CleaningShiftAdapter(
    private val cleaningShiftViewModel: CleaningShiftViewModel,
    private val editCleaningShift: (CleaningShift) -> Unit
) : ListAdapter<CleaningShift, CleaningShiftAdapter.CleaningShiftViewHolder>(DiffCallback()) {

    private var editingShiftId: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CleaningShiftViewHolder {
        val binding = ItemCleaningShiftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CleaningShiftViewHolder(binding, cleaningShiftViewModel, editCleaningShift)
    }

    override fun onBindViewHolder(holder: CleaningShiftViewHolder, position: Int) {
        val cleaningShift = getItem(position)
        holder.bind(cleaningShift, editingShiftId)
    }

    fun setEditingShiftId(id: Int?) {
        editingShiftId = id
        notifyDataSetChanged()
    }

    class CleaningShiftViewHolder(
        private val binding: ItemCleaningShiftBinding,
        private val cleaningShiftViewModel: CleaningShiftViewModel,
        private val editCleaningShift: (CleaningShift) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cleaningShift: CleaningShift, editingShiftId: Int?) {
            binding.cleaningShift = cleaningShift
            binding.executePendingBindings()

            if (cleaningShift.id == editingShiftId) {
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.editing_background))
            } else {
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.transparent))
            }

            binding.root.setOnClickListener {
                editCleaningShift(cleaningShift)
            }

            binding.buttonDelete.setOnClickListener {
                cleaningShiftViewModel.delete(cleaningShift)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CleaningShift>() {
        override fun areItemsTheSame(oldItem: CleaningShift, newItem: CleaningShift): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CleaningShift, newItem: CleaningShift): Boolean {
            return oldItem == newItem
        }
    }
}
