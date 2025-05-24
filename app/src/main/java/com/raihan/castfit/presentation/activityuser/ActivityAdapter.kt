package com.raihan.castfit.presentation.activityuser

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.raihan.castfit.data.model.ProgressActivity
import com.raihan.castfit.databinding.LayoutActivityOnProgressBinding

class ActivityAdapter(
    //private val listener: (ProgressActivity, Int) -> Unit
    private val onCancelClick: (ProgressActivity, Int) -> Unit,
    private val onFinishClick: (ProgressActivity, Int) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.ItemProgressViewHolder>() {

    private val differ = AsyncListDiffer(this,
        object : DiffUtil.ItemCallback<ProgressActivity>() {
            override fun areItemsTheSame(
                oldItem: ProgressActivity,
                newItem: ProgressActivity
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ProgressActivity,
                newItem: ProgressActivity
            ): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        })

    fun setData(data: List<ProgressActivity>) {
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

    /*override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemProgressViewHolder {
        val binding = LayoutActivityOnProgressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemProgressViewHolder(binding) { activity, position ->
            listener(activity, position)
        }
    }*/

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemProgressViewHolder {
        val binding = LayoutActivityOnProgressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemProgressViewHolder(binding, onCancelClick, onFinishClick)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(
        holder: ItemProgressViewHolder,
        position: Int
    ) {
        holder.bind(differ.currentList[position], position)
    }

    class ItemProgressViewHolder(
        private val binding: LayoutActivityOnProgressBinding,
        //val itemClick: (ProgressActivity, Int) -> Unit
        private val onCancelClick: (ProgressActivity, Int) -> Unit,
        private val onFinishClick: (ProgressActivity, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProgressActivity, position: Int) {
            with(item) {
                binding.tvActivityProgress.text = physicalActivityName
                binding.btnCancelActivity.setOnClickListener {
                    //itemClick(this, position)
                    onCancelClick(this, position)
                }
                binding.btnFinishingActivity.setOnClickListener {
                    onFinishClick(this, position)
                }
            }
        }
    }
}