package com.example.taskmasterfinalproject.details

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmasterfinalproject.databinding.ItemSubtaskBinding
import com.example.taskmasterfinalproject.model.Subtask

class SubtaskAdapter(
    private val onSubtaskToggled: (Subtask) -> Unit,
    private val onSubtaskDeleted: (Subtask) -> Unit
) :
    ListAdapter<Subtask, SubtaskAdapter.SubtaskViewHolder>(SubtaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskViewHolder {
        val binding = ItemSubtaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubtaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubtaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubtaskViewHolder(private val binding: ItemSubtaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(subtask: Subtask) {
            binding.checkboxSubtask.text = subtask.title
            binding.checkboxSubtask.isChecked = subtask.isDone

            if (subtask.isDone) {
                binding.checkboxSubtask.paintFlags = binding.checkboxSubtask.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.checkboxSubtask.paintFlags = binding.checkboxSubtask.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            binding.checkboxSubtask.setOnClickListener {
                onSubtaskToggled(subtask)
            }
            
            binding.buttonDeleteSubtask.setOnClickListener {
                onSubtaskDeleted(subtask)
            }
        }
    }

    private class SubtaskDiffCallback : DiffUtil.ItemCallback<Subtask>() {
        override fun areItemsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
            return oldItem == newItem
        }
    }
}
