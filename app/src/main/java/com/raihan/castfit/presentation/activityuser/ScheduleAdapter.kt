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

// DateHeaderItem - sudah benar, tapi saya tambahkan beberapa improvement
class DateHeaderItem(private val date: String) : BindableItem<LayoutDateHeaderScheduledBinding>() {
    override fun getLayout() = R.layout.layout_date_header_scheduled

    override fun bind(viewBinding: LayoutDateHeaderScheduledBinding, position: Int) {
        viewBinding.tvDateScheduled.text = date
    }

    override fun initializeViewBinding(view: View): LayoutDateHeaderScheduledBinding {
        return LayoutDateHeaderScheduledBinding.bind(view)
    }

    // Tambahkan untuk membedakan item yang sama
    override fun isSameAs(other: Item<*>): Boolean {
        return other is DateHeaderItem && other.date == date
    }

    override fun hasSameContentAs(other: Item<*>): Boolean {
        return other is DateHeaderItem && other.date == date
    }
}

// ScheduleItem - perlu diperbaiki berdasarkan scheduledAdapter
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

            // Tombol continue hanya muncul jika tanggal adalah hari ini
            // (sama seperti di scheduledAdapter yang di-comment)
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

    // Tambahkan untuk performa dan diferensiasi item
    override fun isSameAs(other: Item<*>): Boolean {
        return other is ScheduleItem && other.schedule.id == schedule.id
    }

    override fun hasSameContentAs(other: Item<*>): Boolean {
        return other is ScheduleItem && other.schedule.hashCode() == schedule.hashCode()
    }

    // Method untuk mengakses schedule dari luar (diperlukan untuk adapter)
    fun getSchedule(): ScheduleActivity = schedule

    // Method formatDateToDisplay sama seperti di scheduledAdapter
    private fun formatDateToDisplay(dateString: String?): String {
        return try {
            if (dateString.isNullOrEmpty()) return ""

            // Parse dari format yyyy-MM-dd
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

// ScheduleGroupieAdapter yang menggunakan item-item di atas
class ScheduleGroupieAdapter(
    private val onCancelClick: (ScheduleActivity, Int) -> Unit,
    private val onFinishClick: (ScheduleActivity, Int) -> Unit
) {

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private var scheduleList = mutableListOf<ScheduleActivity>()

    fun getAdapter(): GroupAdapter<GroupieViewHolder> = groupAdapter

    fun setData(data: List<ScheduleActivity>) {
        Log.d("ScheduleGroupieAdapter", "Setting data with ${data.size} items")
        scheduleList.clear()
        scheduleList.addAll(data)
        updateGroupedData()
    }

    fun removeItem(position: Int) {
        // Cari schedule berdasarkan position di list asli
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

    /*private fun updateGroupedData() {
        val groupedItems = mutableListOf<Item<*>>()

        // Group schedules by date, sama seperti logika di scheduledAdapter
        val groupedByDate = scheduleList
            .sortedBy { it.dateScheduled }
            .groupBy { it.dateScheduled }

        Log.d("ScheduleGroupieAdapter", "Grouped by ${groupedByDate.size} dates")

        groupedByDate.forEach { (date, schedules) ->
            // Add date header
            val formattedDate = formatDateHeader(date)
            groupedItems.add(DateHeaderItem(formattedDate))

            // Add schedule items for this date
            schedules.forEach { schedule ->
                val originalPosition = scheduleList.indexOfFirst { it.id == schedule.id }
                val scheduleItem = ScheduleItem(
                    schedule = schedule,
                    onCancelClick = onCancelClick,
                    onFinishClick = onFinishClick
                )
                groupedItems.add(scheduleItem)
            }
        }

        Log.d("ScheduleGroupieAdapter", "Total grouped items: ${groupedItems.size}")
        groupAdapter.updateAsync(groupedItems)
    }*/

    private fun updateGroupedData() {
        val groupedItems = mutableListOf<Item<*>>()

        val today = Calendar.getInstance()

        val (pastSchedules, upcomingSchedules) = scheduleList.partition {
            val scheduleDate = Calendar.getInstance().apply {
                time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.dateScheduled) ?: Date()
            }
            scheduleDate.before(today) && !isSameDay(scheduleDate, today)
        }

        // Fungsi untuk mengelompokkan dan membuat item Groupie per tanggal
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

        // Urutan: upcoming & today dulu, lalu past
        buildGroupedItems(upcomingSchedules)
        //buildGroupedItems(pastSchedules)
        buildGroupedItems(pastSchedules.sortedByDescending { it.dateScheduled })

        Log.d("ScheduleGroupieAdapter", "Total grouped items: ${groupedItems.size}")
        groupAdapter.updateAsync(groupedItems)
    }

    /*private fun updateGroupedData() {
        val groupedItems = mutableListOf<Item<*>>()
        val today = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Buat list sementara dan filter yang sudah lewat 14 hari
        val expiredSchedules = mutableListOf<ScheduleActivity>()
        val validSchedules = scheduleList.filter { schedule ->
            try {
                val scheduleDate = Calendar.getInstance().apply {
                    time = dateFormat.parse(schedule.dateScheduled) ?: return@filter false
                }
                val diff = today.timeInMillis - scheduleDate.timeInMillis
                val daysDiff = TimeUnit.MILLISECONDS.toDays(diff)

                if (daysDiff > 14) {
                    expiredSchedules.add(schedule)
                    false // exclude from display
                } else {
                    true
                }
            } catch (e: Exception) {
                false
            }
        }

        // Hapus yang expired dari database
        expiredSchedules.forEach { expired ->
            removeSchedule(expired) // Implementasikan sesuai Room
        }

        // Update scheduleList agar tidak menampilkan yang dihapus
        scheduleList.clear()
        scheduleList.addAll(validSchedules)

        // Pisahkan ke yang akan datang & sudah lewat
        val (pastSchedules, upcomingSchedules) = validSchedules.partition {
            val scheduleDate = Calendar.getInstance().apply {
                time = dateFormat.parse(it.dateScheduled) ?: Date()
            }
            scheduleDate.before(today) && !isSameDay(scheduleDate, today)
        }

        fun buildGroupedItems(data: List<ScheduleActivity>) {
            val grouped = data.sortedBy { it.dateScheduled }.groupBy { it.dateScheduled }
            grouped.forEach { (date, schedules) ->
                val formattedDate = formatDateHeader(date)
                groupedItems.add(DateHeaderItem(formattedDate))
                schedules.forEach { schedule ->
                    groupedItems.add(
                        ScheduleItem(
                            schedule = schedule,
                            onCancelClick = onCancelClick,
                            onFinishClick = onFinishClick
                        )
                    )
                }
            }
        }

        buildGroupedItems(upcomingSchedules)
        buildGroupedItems(pastSchedules.sortedByDescending { it.dateScheduled })

        groupAdapter.updateAsync(groupedItems)
    }*/


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
/*
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

    fun getItemPosition(schedule: ScheduleActivity): Int {
        return differ.currentList.indexOfFirst { it.id == schedule.id }
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
                */
/*binding.btnContinueScheduled.setOnClickListener {
                    onFinishClick(this, position)
                }*//*

                // Tombol continue hanya muncul jika tanggal adalah hari ini
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                if (dateScheduled == today) {
                    binding.btnContinueScheduled.visibility = View.VISIBLE
                    binding.btnContinueScheduled.setOnClickListener {
                        onFinishClick(this, position)
                    }
                } else {
                    binding.btnContinueScheduled.visibility = View.GONE
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
}*/
