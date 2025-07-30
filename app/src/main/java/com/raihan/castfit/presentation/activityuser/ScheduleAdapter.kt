package com.raihan.castfit.presentation.activityuser

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.raihan.castfit.R
import com.raihan.castfit.data.model.ScheduleActivity
import com.raihan.castfit.databinding.LayoutDateHeaderScheduledBinding
import com.raihan.castfit.databinding.LayoutScheduledActivityBinding
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DateHeaderItem(private val date: String) : BindableItem<LayoutDateHeaderScheduledBinding>() {
    override fun getLayout() = R.layout.layout_date_header_scheduled

    override fun bind(viewBinding: LayoutDateHeaderScheduledBinding, position: Int) {
        viewBinding.tvDateScheduled.text = date
    }

    override fun initializeViewBinding(view: View): LayoutDateHeaderScheduledBinding {
        return LayoutDateHeaderScheduledBinding.bind(view)
    }

    override fun isSameAs(other: Item<*>): Boolean {
        return other is DateHeaderItem && other.date == date
    }

    override fun hasSameContentAs(other: Item<*>): Boolean {
        return other is DateHeaderItem && other.date == date
    }
}

class ScheduleItem(
    private val schedule: ScheduleActivity,
    private val onCancelClick: (ScheduleActivity, Int) -> Unit,
    private val onFinishClick: (ScheduleActivity, Int) -> Unit
) : BindableItem<LayoutScheduledActivityBinding>() {

    override fun getLayout() = R.layout.layout_scheduled_activity

    override fun bind(viewBinding: LayoutScheduledActivityBinding, position: Int) {
        with(schedule) {
            viewBinding.tvScheduledList.text = physicalActivityName
            viewBinding.tvDateScheduled.text = formatDateToDisplay(dateScheduled)

            viewBinding.btnCancelScheduled.setOnClickListener {
                onCancelClick(this, position)
            }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            if (dateScheduled == today) {
                viewBinding.btnContinueScheduled.visibility = View.VISIBLE
                viewBinding.btnContinueScheduled.setOnClickListener {
                    onFinishClick(this, position)
                }
            } else {
                viewBinding.btnContinueScheduled.visibility = View.GONE
            }
        }
    }

    override fun initializeViewBinding(view: View): LayoutScheduledActivityBinding {
        return LayoutScheduledActivityBinding.bind(view)
    }

    override fun isSameAs(other: Item<*>): Boolean {
        return other is ScheduleItem && other.schedule.id == schedule.id
    }

    override fun hasSameContentAs(other: Item<*>): Boolean {
        return other is ScheduleItem && other.schedule.hashCode() == schedule.hashCode()
    }

    fun getSchedule(): ScheduleActivity = schedule

    private fun formatDateToDisplay(dateString: String?): String {
        return try {
            if (dateString.isNullOrEmpty()) return ""

            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            Log.e("ScheduleItem", "Error formatting date: $dateString", e)
            dateString ?: ""
        }
    }
}

class ScheduleGroupieAdapter(
    private val onCancelClick: (ScheduleActivity, Int) -> Unit,
    private val onFinishClick: (ScheduleActivity, Int) -> Unit
) {

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private var scheduleList = mutableListOf<ScheduleActivity>()

    fun getAdapter(): GroupAdapter<GroupieViewHolder> = groupAdapter

    fun getScheduleList(): List<ScheduleActivity> = scheduleList

    fun setData(data: List<ScheduleActivity>) {
        Log.d("ScheduleGroupieAdapter", "Setting data with ${data.size} items")
        scheduleList.clear()
        scheduleList.addAll(data)
        updateGroupedData()
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < scheduleList.size) {
            scheduleList.removeAt(position)
            updateGroupedData()
        }
    }

    fun removeSchedule(schedule: ScheduleActivity) {
        scheduleList.removeAll { it.id == schedule.id }
        updateGroupedData()
    }

    fun getItemPosition(schedule: ScheduleActivity): Int {
        return scheduleList.indexOfFirst { it.id == schedule.id }
    }

    private fun updateGroupedData() {
        val groupedItems = mutableListOf<Item<*>>()

        val today = Calendar.getInstance()

        val (pastSchedules, upcomingSchedules) = scheduleList.partition {
            val scheduleDate = Calendar.getInstance().apply {
                time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.dateScheduled) ?: Date()
            }
            scheduleDate.before(today) && !isSameDay(scheduleDate, today)
        }

        fun buildGroupedItems(data: List<ScheduleActivity>) {
            val grouped = data.sortedBy { it.dateScheduled }.groupBy { it.dateScheduled }

            grouped.forEach { (date, schedules) ->
                val formattedDate = formatDateHeader(date)
                groupedItems.add(DateHeaderItem(formattedDate))

                schedules.forEach { schedule ->
                    val scheduleItem = ScheduleItem(
                        schedule = schedule,
                        onCancelClick = onCancelClick,
                        onFinishClick = onFinishClick
                    )
                    groupedItems.add(scheduleItem)
                }
            }
        }

        buildGroupedItems(upcomingSchedules)
        buildGroupedItems(pastSchedules.sortedByDescending { it.dateScheduled })

        Log.d("ScheduleGroupieAdapter", "Total grouped items: ${groupedItems.size}")
        groupAdapter.updateAsync(groupedItems)
    }

    private fun formatDateHeader(dateString: String?): String {
        return try {
            if (dateString.isNullOrEmpty()) return "Tanggal Tidak Valid"

            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateString) ?: return dateString

            val today = Calendar.getInstance()
            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val scheduleDate = Calendar.getInstance().apply { time = date }

            when {
                isSameDay(scheduleDate, today) -> "Hari Ini"
                isSameDay(scheduleDate, tomorrow) -> "Besok"
                else -> {
                    val outputFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                    outputFormat.format(date)
                }
            }
        } catch (e: Exception) {
            Log.e("ScheduleGroupieAdapter", "Error formatting date header: $dateString", e)
            dateString ?: "Tanggal Tidak Valid"
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
