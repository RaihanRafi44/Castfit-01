package com.raihan.castfit.presentation.activityuser

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.raihan.castfit.R
import com.raihan.castfit.data.model.HistoryActivity
import com.raihan.castfit.databinding.LayoutActivityHistoryBinding
import com.raihan.castfit.databinding.LayoutDateHeaderHistoryBinding
import com.raihan.castfit.presentation.activityuser.ActivityAdapter.ItemProgressViewHolder
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
    private val listener: (HistoryItem, HistoryActivity) -> Unit
) : BindableItem<LayoutActivityHistoryBinding>() {

    override fun bind(viewBinding: LayoutActivityHistoryBinding, position: Int) {
        viewBinding.tvActivityHistory.text = historyActivity.physicalActivityName ?: "Unknown Activity"
        viewBinding.tvDate.text = historyActivity.dateEnded
        viewBinding.tvDuration.text = "${historyActivity.duration ?: 0} menit"
        viewBinding.btnCancelActivity.setOnClickListener {
            listener(this, historyActivity) // Kirim referensi item + data
        }
    }

    override fun getLayout() = R.layout.layout_activity_history

    override fun initializeViewBinding(view: View): LayoutActivityHistoryBinding {
        return LayoutActivityHistoryBinding.bind(view)
    }
}
