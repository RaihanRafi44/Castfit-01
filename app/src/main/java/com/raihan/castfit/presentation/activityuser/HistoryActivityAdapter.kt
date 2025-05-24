package com.raihan.castfit.presentation.activityuser

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.raihan.castfit.data.model.HistoryActivity
import com.raihan.castfit.databinding.LayoutActivityHistoryBinding
import com.raihan.castfit.presentation.activityuser.ActivityAdapter.ItemProgressViewHolder

class HistoryActivityAdapter(
    private val listener: (HistoryActivity, Int) -> Unit
) : RecyclerView.Adapter<HistoryActivityAdapter.ItemHistoryViewHolder>() {

    private val differ = AsyncListDiffer(this,
        object : DiffUtil.ItemCallback<HistoryActivity>() {
            override fun areItemsTheSame(
                oldItem: HistoryActivity,
                newItem: HistoryActivity
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: HistoryActivity,
                newItem: HistoryActivity
            ): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        })

    fun setData(data: List<HistoryActivity>) {
        differ.submitList(data)
    }

    fun removeItem(position: Int) {
        val currentList = differ.currentList.toMutableList()
        if (position >= 0 && position < currentList.size) {
            currentList.removeAt(position)
            differ.submitList(currentList.toList())
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemHistoryViewHolder {
        val binding = LayoutActivityHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        //return ItemHistoryViewHolder(binding, listener)
        return ItemHistoryViewHolder(binding) { activity, position ->
            listener(activity, position)
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(
        holder: ItemHistoryViewHolder,
        position: Int
    ) {
        holder.bind(differ.currentList[position], position)
    }

    class ItemHistoryViewHolder(
        private val binding: LayoutActivityHistoryBinding,
        val itemClick: (HistoryActivity, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryActivity, position: Int) {
            with(item) {
                binding.tvActivityHistory.text = physicalActivityName?.ifEmpty { "Unknown Activity" }
                binding.tvDate.text = dateEnded
                binding.tvDuration.text = "${duration ?: 0} menit"
                binding.btnCancelActivity.setOnClickListener {
                    itemClick(this, position)
                }
            }
        }
    }
}