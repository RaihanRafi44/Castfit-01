package com.raihan.castfit.presentation.activityuser

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.raihan.castfit.data.model.ScheduleActivity
import com.raihan.castfit.databinding.LayoutScheduledActivityBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ScheduleAdapter(
    private val onCancelClick: (ScheduleActivity, Int) -> Unit,
    private val onFinishClick: (ScheduleActivity, Int) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ItemScheduleViewHolder>() {

    private val differ = AsyncListDiffer(this,
        object : DiffUtil.ItemCallback<ScheduleActivity>() {
            override fun areItemsTheSame(
                oldItem: ScheduleActivity,
                newItem: ScheduleActivity
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ScheduleActivity,
                newItem: ScheduleActivity
            ): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        })

    fun setData(data: List<ScheduleActivity>) {
        Log.d("ActivityAdapter", "Submit list size: ${data.size}")
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
    ): ItemScheduleViewHolder {
        val binding = LayoutScheduledActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemScheduleViewHolder(binding, onCancelClick, onFinishClick)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(
        holder: ItemScheduleViewHolder,
        position: Int
    ) {
        holder.bind(differ.currentList[position], position)
    }

    class ItemScheduleViewHolder(
        private val binding: LayoutScheduledActivityBinding,
        private val onCancelClick: (ScheduleActivity, Int) -> Unit,
        private val onFinishClick: (ScheduleActivity, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScheduleActivity, position: Int) {
            with(item) {
                binding.tvScheduledList.text = physicalActivityName
                binding.tvDateScheduled.text = formatDateToDisplay(dateScheduled)
                binding.btnCancelScheduled.setOnClickListener {
                    onCancelClick(this, position)
                }
                binding.btnContinueScheduled.setOnClickListener {
                    onFinishClick(this, position)
                }
            }
        }

        private fun formatDateToDisplay(dateString: String?): String {
            return try {
                if (dateString.isNullOrEmpty()) return ""

                // Parse dari format yyyy-MM-dd
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

                val date = inputFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                Log.e("ScheduleAdapter", "Error formatting date: $dateString", e)
                dateString ?: ""
            }
        }
    }
}