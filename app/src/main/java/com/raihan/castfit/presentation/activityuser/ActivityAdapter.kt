package com.raihan.castfit.presentation.activityuser

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.raihan.castfit.R
import com.raihan.castfit.data.model.ProgressActivity
import com.raihan.castfit.databinding.LayoutActivityOnProgressBinding

class ActivityAdapter(
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
        private val onCancelClick: (ProgressActivity, Int) -> Unit,
        private val onFinishClick: (ProgressActivity, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProgressActivity, position: Int) {
            with(item) {
                binding.tvActivityProgress.text = physicalActivityName
                binding.tvStartTime.text = "Dimulai pukul $startedAt"

                binding.btnCancelActivity.setOnClickListener {
                    onCancelClick(this, position)
                }
                binding.btnFinishingActivity.setOnClickListener {
                    onFinishClick(this, position)
                }

                // Set icon based on activity name
                val iconRes = getActivityIcon(physicalActivityName)
                binding.ivActivityIcon.setImageResource(iconRes)
            }
        }

        private fun getActivityIcon(name: String?): Int {
            return when (name) {
                "Jogging" -> R.drawable.img_jogging
                "Walking" -> R.drawable.img_walking
                "Cycling" -> R.drawable.img_cycling
                "Senam Otak" -> R.drawable.img_brain
                "Senam Poco-Poco" -> R.drawable.img_exercise
                "Senam Lansia" -> R.drawable.img_elderly
                "Yoga" -> R.drawable.img_yoga
                "Jumping Jack" -> R.drawable.img_jumping_jack
                "Push Up" -> R.drawable.img_push_up
                "Sit up" -> R.drawable.img_sit_up
                "Crunch" -> R.drawable.img_crunch_man
                "Rusian Twist" -> R.drawable.img_russian_twist
                "Back Up" -> R.drawable.img_back_up
                "Squat" -> R.drawable.img_squat
                "Step Up Down" -> R.drawable.img_step_up_down
                "Front Lunges" -> R.drawable.img_front_lunges
                "Wall Seat" -> R.drawable.img_wall_sit
                "Plank" -> R.drawable.img_plank
                else -> R.drawable.ic_activity // Fallback
            }
        }
    }
}