package com.management.roomates.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.management.roomates.R
import com.management.roomates.data.model.CleaningShift
import com.management.roomates.data.model.Update
import com.management.roomates.databinding.ItemCleaningShiftBinding
import com.management.roomates.databinding.ItemUpdateBinding
import com.management.roomates.viewmodel.CleaningShiftViewModel
import com.management.roomates.viewmodel.UpdatesViewModel

class UpdatesAdapter(
    private val updatesViewModel: UpdatesViewModel,
    private val editUpdate: (Update) -> Unit
) : ListAdapter<Update, UpdatesAdapter.UpdatesViewHolder>(DiffCallback()) {

    private var updateId: Int? = null

    private var listener:OnItemClickListener?=null

    interface OnItemClickListener{
        fun deleteClick(update: Update)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpdatesViewHolder {
        val binding = ItemUpdateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UpdatesViewHolder(binding, updatesViewModel, editUpdate,listener!!)
    }

    override fun onBindViewHolder(holder: UpdatesViewHolder, position: Int) {
        val update = getItem(position)
        holder.bind(update, updateId)
    }

    fun setUpdateId(id: Int?) {
        updateId = id
        notifyDataSetChanged()
    }

    class UpdatesViewHolder(
        private val binding: ItemUpdateBinding,
        private val updatesViewModel: UpdatesViewModel,
        private val editUpdate: (Update) -> Unit,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(update: Update, editingUpdateId: Int?) {
            binding.update = update
            binding.executePendingBindings()

//            if (update.id == editingUpdateId) {
//                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.editing_background))
//            } else {
//                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.transparent))
//            }

//            binding.root.setOnClickListener {
//                editCleaningShift(cleaningShift)
//            }

            binding.buttonDelete.setOnClickListener {
                mListener.deleteClick(update)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Update>() {
        override fun areItemsTheSame(oldItem: Update, newItem: Update): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Update, newItem: Update): Boolean {
            return oldItem == newItem
        }
    }
}
