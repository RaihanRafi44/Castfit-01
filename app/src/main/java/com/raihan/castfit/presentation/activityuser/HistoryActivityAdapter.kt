package com.raihan.castfit.presentation.activityuser

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.raihan.castfit.R
import com.raihan.castfit.data.model.HistoryActivity
import com.raihan.castfit.databinding.LayoutActivityHistoryBinding
import com.raihan.castfit.databinding.LayoutDateHeaderHistoryBinding
import com.raihan.castfit.presentation.activityuser.ActivityAdapter.ItemProgressViewHolder
import com.raihan.castfit.utils.toReadableDate
import com.raihan.castfit.utils.toReadableDuration
import com.xwray.groupie.viewbinding.BindableItem

class DateHeaderHistoryAdapter(private val dateHistory: String) : BindableItem<LayoutDateHeaderHistoryBinding>(){
    override fun bind(
        viewBinding: LayoutDateHeaderHistoryBinding,
        position: Int
    ) {
        viewBinding.tvDateHistory.text = dateHistory
    }

    override fun getLayout() = R.layout.layout_date_header_history

    override fun initializeViewBinding(view: View): LayoutDateHeaderHistoryBinding {
        return LayoutDateHeaderHistoryBinding.bind(view)
    }
}

class HistoryItem(
    private val historyActivity: HistoryActivity,
    private val activityType: String,
    private val listener: (HistoryItem, HistoryActivity) -> Unit
) : BindableItem<LayoutActivityHistoryBinding>() {

    override fun bind(viewBinding: LayoutActivityHistoryBinding, position: Int) {
        viewBinding.tvActivityHistory.text = historyActivity.physicalActivityName ?: "Unknown Activity"
        viewBinding.tvDate.text = historyActivity.dateEnded?.toReadableDate()
        viewBinding.tvDuration.text = historyActivity.duration?.toReadableDuration() ?: "0 detik"
        viewBinding.btnCancelActivity.setOnClickListener {
            listener(this, historyActivity) // Kirim referensi item + data
        }

        // Use icon based on activity name
        val iconRes = getActivityIcon(historyActivity.physicalActivityName)
        viewBinding.ivActivityIcon.setImageResource(iconRes)

        // Use solid light pastel colors for consistency (avoiding transparency bugs)
        val cardColor = if (activityType == "Indoor") "#F3E5F5" else "#E0F2F1"
        viewBinding.root.setCardBackgroundColor(Color.parseColor(cardColor))

        // Set text colors explicitly to ensure they don't inherit recycled values
        viewBinding.tvActivityHistory.setTextColor(Color.parseColor("#212121"))
        viewBinding.tvDate.setTextColor(Color.parseColor("#757575"))
        viewBinding.tvDuration.setTextColor(Color.parseColor("#424242"))
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

    override fun getLayout() = R.layout.layout_activity_history

    override fun initializeViewBinding(view: View): LayoutActivityHistoryBinding {
        return LayoutActivityHistoryBinding.bind(view)
    }
}
