package com.raihan.castfit.presentation.recommendation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.raihan.castfit.data.model.PhysicalActivity
import com.raihan.castfit.databinding.LayoutOutdoorSportsBinding

class RecommendationOutdoorAdapter (
    private val listener: (PhysicalActivity) -> Unit
) : RecyclerView.Adapter<RecommendationOutdoorAdapter.ItemRecommendationViewHolder>(){

    private val differ = AsyncListDiffer(this,
        object : DiffUtil.ItemCallback<PhysicalActivity>() {
            override fun areItemsTheSame(
                oldItem: PhysicalActivity,
                newItem: PhysicalActivity
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: PhysicalActivity,
                newItem: PhysicalActivity
            ) : Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        })

    fun setData(data: List<PhysicalActivity>){
        differ.submitList(data)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemRecommendationViewHolder {
        val binding = LayoutOutdoorSportsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemRecommendationViewHolder(binding, listener)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(
        holder: ItemRecommendationViewHolder,
        position: Int
    ) {
        holder.bind(differ.currentList[position])
    }

    class ItemRecommendationViewHolder(
        private val binding: LayoutOutdoorSportsBinding,
        val itemClick: (PhysicalActivity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: PhysicalActivity){
            with(item){
                binding.tvOutdoorList.text = name
                binding.root.setOnClickListener { itemClick(this) }
            }
        }
    }
}