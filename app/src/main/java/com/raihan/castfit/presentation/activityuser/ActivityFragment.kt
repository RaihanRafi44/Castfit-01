package com.raihan.castfit.presentation.activityuser

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.raihan.castfit.R
import com.raihan.castfit.data.model.HistoryActivity
import com.raihan.castfit.data.model.PhysicalActivity
import com.raihan.castfit.data.model.ProgressActivity
import com.raihan.castfit.data.model.ScheduleActivity
import com.raihan.castfit.databinding.FragmentActivityBinding
import com.raihan.castfit.presentation.home.HomeViewModel
import com.raihan.castfit.utils.NotificationHelper
import com.raihan.castfit.utils.NotificationReceiver
import com.raihan.castfit.utils.proceedWhen
import com.raihan.castfit.utils.toReadableDate
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ActivityFragment : Fragment() {

    private lateinit var binding: FragmentActivityBinding
    private val activityPhysicalViewModel: ActivityViewModel by viewModel()
    private val homeViewModel: HomeViewModel by activityViewModel()
    //private val homeViewModel: HomeViewModel by viewModel()
    private var selectedSchedulePosition: Int? = null

    private var isCheckingProgress = false

    // Timer variables
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var currentProgressActivity: ProgressActivity? = null
    private var timerStartTime: Long = 0L

    // Dropdown Adapter
    private var isScheduledExpanded = false
    private var isOnProgressExpanded = false
    private var isHistoryExpanded = false

    /*private val scheduledAdapter = ScheduleAdapter(
        onCancelClick = { schedule, position ->
            showDeleteScheduledDialog(schedule, position)
        },
        onFinishClick = { schedule, position ->
            showStartScheduledActivityDialog(schedule, position)
        }
    )*/

    // Ganti scheduledAdapter dengan scheduleGroupieAdapter
    private val scheduleGroupieAdapter = ScheduleGroupieAdapter(
        onCancelClick = { schedule, position ->
            showDeleteScheduledDialog(schedule, position)
        },
        onFinishClick = { schedule, position ->
            showStartScheduledActivityDialog(schedule, position)
        }
    )

    private val progressAdapter = ActivityAdapter(
        onCancelClick = { activity, position ->
            showCancelDialog(activity, position)
        },
        onFinishClick = { activity, position ->
            showFinishDialog(activity, position)
        }
    )

    /*private val historyAdapter = HistoryActivityAdapter { history, position ->
        showDeleteHistoryDialog(history, position)
    }*/

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private var currentWeatherCondition: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentActivityBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }*/

        refreshWeatherData()
        observeCurrentWeather()
        observeWeatherCheckResult()
        setupRecyclerViews()
        observeData()
        observeDeleteResult()
        observeFinishResult()
        observeDeleteHistoryResult()
        observeDeleteScheduleResult()
        observeStartScheduledActivityResult()
        loadWeatherData()

        binding.mcwScheduledTitle.setOnClickListener {
            isScheduledExpanded = !isScheduledExpanded
            binding.rvScheduledList.visibility = if (isScheduledExpanded) View.VISIBLE else View.GONE
            binding.ivArrowDropScheduled.rotation = if (isScheduledExpanded) 180f else 0f
        }

        binding.mcwOnProgressTitle.setOnClickListener {
            isOnProgressExpanded = !isOnProgressExpanded
            binding.rvOnProgressList.visibility = if (isOnProgressExpanded) View.VISIBLE else View.GONE
            binding.ivArrowDropProgress.rotation = if (isOnProgressExpanded) 180f else 0f
        }

        binding.mcwHistoryTitle.setOnClickListener {
            isHistoryExpanded = !isHistoryExpanded
            binding.rvHistoryList.visibility = if (isHistoryExpanded) View.VISIBLE else View.GONE
            binding.ivArrowDropHistory.rotation = if (isHistoryExpanded) 180f else 0f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimeCounter()
    }

    private fun refreshWeatherData() {
        Log.d("ActivityFragment", "Attempting to refresh weather data")
        try {
            // Load saved location which should trigger weather fetch
            homeViewModel.loadSavedLocation()

            // If there's a current location, we might need to fetch weather
            homeViewModel.currentLocation.value?.currentLocation?.let { location ->
                if (location.latitude != null && location.longitude != null) {
                    Log.d("ActivityFragment", "Location available for weather refresh: ${location.latitude}, ${location.longitude}")
                    // Weather should be fetched automatically by HomeViewModel when location is available
                } else {
                    Log.w("ActivityFragment", "Location coordinates not available for weather refresh")
                }
            } ?: Log.w("ActivityFragment", "No current location available for weather refresh")
        } catch (e: Exception) {
            Log.e("ActivityFragment", "Error refreshing weather data", e)
        }
    }

    private fun loadWeatherData() {
        Log.d("ActivityFragment", "Loading weather data...")
        // Load saved location first
        homeViewModel.loadSavedLocation()

        // Observe current location to trigger weather fetch if needed
        homeViewModel.currentLocation.observe(viewLifecycleOwner) { locationState ->
            Log.d("ActivityFragment", "Location state: isLoading=${locationState?.isLoading}, location=${locationState?.currentLocation != null}")

            locationState?.currentLocation?.let { location ->
                if (location.latitude != null && location.longitude != null) {
                    Log.d("ActivityFragment", "Location available, checking if weather data exists")
                    // Check if weather data is already available
                    if (currentWeatherCondition == null) {
                        Log.d("ActivityFragment", "Weather data not available, requesting fresh data")
                        // If no weather data, you might need to trigger weather fetch
                        // This depends on your HomeViewModel implementation
                    }
                }
            }
        }
    }

    // Home View Model
    private fun observeCurrentWeather() {
        homeViewModel.weather.observe(viewLifecycleOwner) { weatherUiState ->
            Log.d("ActivityFragment", "Observed weatherUiState: $weatherUiState")
            weatherUiState?.data?.current?.condition?.text?.let { condition ->
                currentWeatherCondition = condition
                Log.d("ActivityFragment", "Current weather condition updated: $condition")
            }
        }

    }

    private fun observeData() {
        // Observe schedule data
        activityPhysicalViewModel.getAllSchedule().observe(viewLifecycleOwner) { result ->
            result.proceedWhen(
                doOnSuccess = {
                    //binding.rvScheduledList.isVisible = true
                    if (isScheduledExpanded) binding.rvScheduledList.visibility = View.VISIBLE
                    it.payload?.let { list ->
                        Log.d("ActivityFragment", "Data schedule size: ${list.size}")
                        //scheduledAdapter.setData(list)
                        scheduleGroupieAdapter.setData(list)
                        // Schedule notifikasi untuk semua jadwal yang baru di-load
                        list.forEach { schedule ->
                            scheduleNotification(schedule)
                        }
                    } ?: run{
                        Log.d("ActivityFragment", "Schedule payload null")
                        scheduleGroupieAdapter.setData(emptyList()) // Penting jika list kosong
                    }
                },
                doOnError = {
                    Log.e("ActivityFragment", "Error loading schedule: ${it.exception}")
                    binding.rvScheduledList.isVisible = false
                },
                doOnLoading = {
                    Log.d("ActivityFragment", "Loading schedule data...")
                }
            )
        }

        // Observe progress data
        activityPhysicalViewModel.getAllProgress().observe(viewLifecycleOwner) { result ->
            result.proceedWhen(
                doOnSuccess = {
                    //binding.rvOnProgressList.isVisible = true
                    if (isOnProgressExpanded) binding.rvOnProgressList.visibility = View.VISIBLE
                    it.payload?.let { list ->
                        Log.d("ActivityFragment", "Data on progress size: ${list.size}")
                        progressAdapter.setData(list)
                        if (list.isNotEmpty()){
                            val latestActivity = list.firstOrNull()
                            if (latestActivity != null) {
                                // Check if this is a different activity than current
                                if (currentProgressActivity?.id != latestActivity.id) {
                                    Log.d("ActivityFragment", "Starting timer for new activity: ${latestActivity.physicalActivityName}")
                                    startTimeCounter(latestActivity)
                                }
                                // If same activity, keep current timer running
                            }
                        } else {
                            stopTimeCounter()
                        }
                    } ?: run {
                        Log.d("ActivityFragment", "Progress payload null")
                        stopTimeCounter()
                    }
                },
                doOnError = {
                    Log.e("ActivityFragment", "Error loading progress: ${it.exception}")
                    binding.rvOnProgressList.isVisible = false
                    stopTimeCounter()
                },
                doOnLoading = {
                    Log.d("ActivityFragment", "Loading on progress data...")
                }
            )
        }

        // Observe history data
        /*activityPhysicalViewModel.getAllHistory().observe(viewLifecycleOwner) { result ->
            result.proceedWhen(
                doOnSuccess = {
                    //binding.rvHistoryList.isVisible = true
                    if (isHistoryExpanded) binding.rvHistoryList.visibility = View.VISIBLE
                    it.payload?.let { list ->
                        Log.d("ActivityFragment", "Data history size: ${list.size}")
                        historyAdapter.setData(list)
                    } ?: Log.d("ActivityFragment", "History payload null")
                },
                doOnError = {
                    Log.e("ActivityFragment", "Error loading history: ${it.exception}")
                    binding.rvHistoryList.isVisible = false
                },
                doOnLoading = {
                    Log.d("ActivityFragment", "Loading history data...")
                }
            )
        }*/
        activityPhysicalViewModel.getAllHistory().observe(viewLifecycleOwner) { result ->
            result.proceedWhen(
                doOnSuccess = {
                    if (isHistoryExpanded) binding.rvHistoryList.visibility = View.VISIBLE
                    it.payload?.let { list ->
                        val grouped = list.groupBy { history -> history.dateEnded }

                        val items = mutableListOf<Item<*>>()
                        grouped.forEach { (date, activities) ->
                            if (date != null) {
                                items.add(DateHeaderHistoryAdapter(date.toReadableDate()))
                            }
                            activities.forEach { activity ->
                                val historyItem = HistoryItem(activity) { item, selected ->
                                    showDeleteHistoryDialog(item, selected)
                                }
                                items.add(historyItem)
                            }
                        }

                        groupAdapter.update(items)
                    }
                },
                doOnError = {
                    Log.e("ActivityFragment", "Error loading history: ${it.exception}")
                    binding.rvHistoryList.isVisible = false
                },
                doOnLoading = {
                    Log.d("ActivityFragment", "Loading history data...")
                }
            )
        }

    }

    private fun setupRecyclerViews() {
        // Setup scheduled RecyclerView
        binding.rvScheduledList.adapter = scheduleGroupieAdapter.getAdapter()
        binding.rvScheduledList.layoutManager = LinearLayoutManager(requireContext())

        // Setup progress RecyclerView
        binding.rvOnProgressList.adapter = progressAdapter
        binding.rvOnProgressList.layoutManager = LinearLayoutManager(requireContext())

        // Setup history RecyclerView
        /*binding.rvHistoryList.adapter = historyAdapter
        binding.rvHistoryList.layoutManager = LinearLayoutManager(requireContext())*/
        binding.rvHistoryList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupAdapter
        }
    }

    private fun showDeleteScheduledDialog(schedule: ScheduleActivity, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Jadwal?")
            .setMessage("Apakah kamu yakin ingin menghapus jadwal aktivitas '${schedule.physicalActivityName}' pada tanggal ${schedule.dateScheduled}?")
            .setPositiveButton("Ya") { _, _ ->
                cancelScheduledNotification(schedule)
                //scheduledAdapter.removeItem(position)
                //scheduleGroupieAdapter.removeItem(position) // Ganti dengan removeItem yang baru
                scheduleGroupieAdapter.removeSchedule(schedule)
                activityPhysicalViewModel.removeSchedule(schedule)
                //scheduledAdapter.removeItem(position)
                Log.d("ActivityLog", "Jadwal '${schedule.physicalActivityName}' akan dihapus dari scheduled list.")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun showStartScheduledActivityDialog(schedule: ScheduleActivity, position: Int) {
        selectedSchedulePosition = position
        Log.d("ActivityFragment", "=== Starting weather check ===")
        Log.d("ActivityFragment", "Checking weather for scheduled activity: ${schedule.physicalActivityName}")
        Log.d("ActivityFragment", "Activity type: ${schedule.physicalActivityType}")
        Log.d("ActivityFragment", "Current weather condition: '$currentWeatherCondition'")

        // Check if weather data is available
        if (currentWeatherCondition.isNullOrEmpty()) {
            Log.w("ActivityFragment", "Weather condition is null or empty")

            // Check weather state for more details
            homeViewModel.weather.value?.let { weatherState ->
                Log.d("ActivityFragment", "Weather state - isLoading: ${weatherState.isLoading}, error: ${weatherState.error}, data exists: ${weatherState.data != null}")

                if (weatherState.isLoading) {
                    Toast.makeText(
                        requireContext(),
                        "Sedang memuat data cuaca, silakan tunggu sebentar...",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                } else if (weatherState.error != null) {
                    Toast.makeText(
                        requireContext(),
                        "Gagal memuat data cuaca: ${weatherState.error}. Silakan periksa koneksi internet dan coba lagi.",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                } else if (weatherState.data?.current?.condition?.text.isNullOrEmpty()) {
                    // Try to refresh weather data
                    refreshWeatherData()
                    Toast.makeText(
                        requireContext(),
                        "Mencoba memuat ulang data cuaca...",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
            } ?: run {
                // No weather state at all, try to load weather data
                refreshWeatherData()
                Toast.makeText(
                    requireContext(),
                    "Memuat data cuaca, silakan tunggu...",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        Log.d("ActivityFragment", "Weather data available, proceeding with weather check")
        // Check weather condition through ViewModel
        activityPhysicalViewModel.checkWeatherForScheduledActivity(schedule, currentWeatherCondition)
    }

    private fun observeWeatherCheckResult() {
        activityPhysicalViewModel.weatherCheckResult.observe(viewLifecycleOwner) { result ->
            result?.let { weatherCheck ->
                if (weatherCheck.isWeatherSuitable) {
                    // ⬇️ CUACA SESUAI: Lanjut ke handleActivitySelection()
                    selectedSchedulePosition = scheduleGroupieAdapter.getItemPosition(weatherCheck.schedule)
                    handleActivitySelection(weatherCheck.schedule)

                } else {
                    // ⬇️ CUACA TIDAK SESUAI: Tampilkan peringatan
                    AlertDialog.Builder(requireContext())
                        .setTitle(weatherCheck.confirmationTitle)
                        .setMessage(weatherCheck.message)
                        .setPositiveButton("Hapus Jadwal") { _, _ ->
                            cancelScheduledNotification(weatherCheck.schedule)
                            activityPhysicalViewModel.removeSchedule(weatherCheck.schedule)
                            selectedSchedulePosition?.let { position ->
                                //scheduleGroupieAdapter.removeItem(position)
                                scheduleGroupieAdapter.removeSchedule(weatherCheck.schedule)
                                Toast.makeText(requireContext(), "Jadwal aktivitas berhasil dihapus", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Nanti Saja", null)
                        .show()
                }
                activityPhysicalViewModel.resetWeatherCheckResult()
            }
        }
    }


    private fun observeStartScheduledActivityResult() {
        activityPhysicalViewModel.startScheduledActivityResult.observe(viewLifecycleOwner) { result ->
            result?.let { success ->
                if (success) {
                    selectedSchedulePosition?.let { position ->
                        val schedule = scheduleGroupieAdapter.getScheduleList().getOrNull(position)
                        schedule?.let {
                            scheduleGroupieAdapter.removeSchedule(it)
                        }
                    }
                    selectedSchedulePosition = null
                    Log.d("ActivityFragment", "Start scheduled activity successful")
                    Toast.makeText(
                        requireContext(),
                        "Aktivitas berhasil dimulai dan dipindahkan ke daftar progress",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e("ActivityFragment", "Start scheduled activity failed")
                    Toast.makeText(
                        requireContext(),
                        "Gagal memulai aktivitas. Silakan coba lagi.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                activityPhysicalViewModel.resetStartScheduledActivityResult()
            }
        }
    }

    private fun showCancelDialog(activity: ProgressActivity, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Batalkan Aktivitas?")
            .setMessage("Apakah kamu yakin ingin membatalkan aktivitas ini?")
            .setPositiveButton("Ya") { _, _ ->
                stopTimeCounter()
                activityPhysicalViewModel.removeProgress(activity)
                progressAdapter.removeItem(position)
                Log.d("ActivityLog", "Aktivitas '${activity.physicalActivityName}' akan dihapus dari halaman Activity.")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    /*private fun showFinishDialog(activity: ProgressActivity, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Selesaikan Aktivitas?")
            .setMessage("Apakah kamu yakin ingin menyelesaikan aktivitas '${activity.physicalActivityName}'? Aktivitas akan dipindahkan ke riwayat.")
            .setPositiveButton("Ya") { _, _ ->
                stopTimeCounter()
                activityPhysicalViewModel.finishActivity(activity)
                progressAdapter.removeItem(position) //Hapus UI list dari progress
                Log.d("ActivityLog", "Aktivitas '${activity.physicalActivityName}' akan dipindahkan ke history.")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }*/

    private fun showFinishDialog(activity: ProgressActivity, position: Int) {
        val startTime = parseStartTime(activity.dateStarted, activity.startedAt)
        if (startTime != null) {
            val durationInMillis = System.currentTimeMillis() - startTime.time
            val durationInSeconds = durationInMillis / 1000

            if (durationInSeconds < 60) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Aktivitas Terlalu Singkat")
                    .setMessage("Aktivitas harus dijalankan minimal 1 menit agar dapat tercatat dalam grafik perkembangan.")
                    .setPositiveButton("OK", null)
                    .show()
                return
            }
        }

        // Jika durasi valid (>= 1 menit), lanjutkan konfirmasi penyelesaian
        AlertDialog.Builder(requireContext())
            .setTitle("Selesaikan Aktivitas?")
            .setMessage("Apakah kamu yakin ingin menyelesaikan aktivitas '${activity.physicalActivityName}'? Aktivitas akan dipindahkan ke riwayat.")
            .setPositiveButton("Ya") { _, _ ->
                stopTimeCounter()
                activityPhysicalViewModel.finishActivity(activity)
                progressAdapter.removeItem(position)
                Log.d("ActivityLog", "Aktivitas '${activity.physicalActivityName}' akan dipindahkan ke history.")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }


    /*private fun showDeleteHistoryDialog(history: HistoryActivity, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Riwayat?")
            .setMessage("Apakah kamu yakin ingin menghapus riwayat aktivitas ini?")
            .setPositiveButton("Ya") { _, _ ->
                activityPhysicalViewModel.removeHistory(history)
                historyAdapter.removeItem(position)
                Log.d("ActivityLog", "Riwayat aktivitas dengan ID '${history.id}' akan dihapus.")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }*/

    private fun showDeleteHistoryDialog(item: HistoryItem, history: HistoryActivity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Riwayat?")
            .setMessage("Apakah kamu yakin ingin menghapus riwayat aktivitas ini?")
            .setPositiveButton("Ya") { _, _ ->
                activityPhysicalViewModel.removeHistory(history)
                groupAdapter.remove(item) // hapus item dari GroupAdapter
                Log.d("ActivityLog", "Riwayat aktivitas dengan ID '${history.id}' akan dihapus.")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }


    private fun observeDeleteResult() {
        activityPhysicalViewModel.deleteOperationResult.observe(viewLifecycleOwner) { result ->
            result?.let { success ->
                if (success) {
                    Log.d("ActivityFragment", "Delete operation successful")
                    Toast.makeText(requireContext(), "Aktivitas berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ActivityFragment", "Delete operation failed")
                    Toast.makeText(requireContext(), "Gagal menghapus aktivitas", Toast.LENGTH_SHORT).show()
                }
                activityPhysicalViewModel.resetDeleteResult()
            }
        }
    }

    private fun observeFinishResult() {
        activityPhysicalViewModel.finishOperationResult.observe(viewLifecycleOwner) { result ->
            result?.let { success ->
                if (success) {
                    Log.d("ActivityFragment", "Finish operation successful")
                    Toast.makeText(requireContext(), "Aktivitas berhasil diselesaikan dan dipindahkan ke riwayat", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ActivityFragment", "Finish operation failed")
                    Toast.makeText(requireContext(), "Gagal menyelesaikan aktivitas", Toast.LENGTH_SHORT).show()
                }
                activityPhysicalViewModel.resetFinishResult()
            }
        }
    }

    private fun observeDeleteHistoryResult() {
        activityPhysicalViewModel.deleteHistoryResult.observe(viewLifecycleOwner) { result ->
            result?.let { success ->
                if (success) {
                    Log.d("ActivityFragment", "Delete history operation successful")
                    Toast.makeText(requireContext(), "Riwayat berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ActivityFragment", "Delete history operation failed")
                    Toast.makeText(requireContext(), "Gagal menghapus riwayat", Toast.LENGTH_SHORT).show()
                }
                activityPhysicalViewModel.resetDeleteHistoryResult()
            }
        }
    }

    private fun observeDeleteScheduleResult() {
        activityPhysicalViewModel.deleteScheduleResult.observe(viewLifecycleOwner) { result ->
            result?.let { success ->
                if (success) {
                    Log.d("ActivityFragment", "Delete schedule operation successful")
                    Toast.makeText(requireContext(), "Jadwal berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ActivityFragment", "Delete schedule operation failed")
                    Toast.makeText(requireContext(), "Gagal menghapus jadwal", Toast.LENGTH_SHORT).show()
                }
                activityPhysicalViewModel.resetDeleteScheduleResult()
            }
        }
    }

    /*private fun scheduleNotification(schedule: ScheduleActivity) {
        val context = requireContext()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val date = try {
            sdf.parse(schedule.dateScheduled)
        } catch (e: Exception) {
            Log.e("NotifDebug", "Date parsing error: ${e.message}")
            null
        }

        if (date == null) {
            Log.e("NotifDebug", "Tanggal null, tidak bisa jadwalkan")
            return
        }

        val startTime = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 4)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endTime = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        while (startTime.before(endTime)) {
            // Lewati kalau waktu sudah lewat sekarang
            if (startTime.timeInMillis <= System.currentTimeMillis()) {
                startTime.add(Calendar.MINUTE, 30)
                continue
            }

            val requestCode = (schedule.id ?: 0) + startTime.get(Calendar.MINUTE) + startTime.get(Calendar.HOUR_OF_DAY) * 100

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("title", "Aktivitas Terjadwal Hari Ini")
                putExtra("message", "Ingat untuk melakukan: ${schedule.physicalActivityName}")
                putExtra("notificationId", requestCode)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        startTime.timeInMillis,
                        pendingIntent
                    )
                } else {
                    val intentSettings = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intentSettings)
                    break // keluar dari loop supaya tidak berlebihan
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    startTime.timeInMillis,
                    pendingIntent
                )
            }

            Log.d("ActivityFragment", "Notifikasi dijadwalkan pada: ${startTime.time}")

            // Tambah 5 menit
            startTime.add(Calendar.MINUTE, 30)
        }
    }

    private fun cancelScheduledNotification(schedule: ScheduleActivity) {
        val context = requireContext()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = try {
            sdf.parse(schedule.dateScheduled)
        } catch (e: Exception) {
            null
        }

        if (date == null) return

        val startTime = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 4)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endTime = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (startTime.before(endTime)) {
            val requestCode = (schedule.id ?: 0) + startTime.get(Calendar.MINUTE) + startTime.get(Calendar.HOUR_OF_DAY) * 100

            val intent = Intent(context, NotificationReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            Log.d("ActivityFragment", "Canceled notification id $requestCode at ${startTime.time}")

            startTime.add(Calendar.MINUTE, 30)
        }
    }*/

    private fun scheduleNotification(schedule: ScheduleActivity) {
        val context = requireContext()

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val date = try {
            sdf.parse(schedule.dateScheduled)
        } catch (e: Exception) {
            null
        }

        if (date == null) return

        val startTime = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 4)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endTime = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        while (startTime.before(endTime)) {
            // Lewati kalau waktu sudah lewat sekarang
            if (startTime.timeInMillis <= System.currentTimeMillis()) {
                startTime.add(Calendar.MINUTE, 30)
                continue
            }

            val requestCode = (schedule.id ?: 0) + startTime.get(Calendar.MINUTE) + startTime.get(Calendar.HOUR_OF_DAY) * 100

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("title", "Aktivitas Terjadwal Hari Ini")
                putExtra("message", "Ingat untuk melakukan: ${schedule.physicalActivityName}")
                putExtra("notificationId", requestCode)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    // 2. Gunakan setAlarmClock untuk prioritas tinggi
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(
                        startTime.timeInMillis,
                        pendingIntent
                    )
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } else {
                    /*val intentSettings = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intentSettings)
                    break*/
                    Log.w("ActivityFragment", "Cannot schedule exact alarms, skipping notification for ${startTime.time}")
                    break
                }
            } else {
                // 3. Untuk Android < 12, gunakan setAlarmClock juga
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(
                        startTime.timeInMillis,
                        pendingIntent
                    )
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        startTime.timeInMillis,
                        pendingIntent
                    )
                }
            }

            Log.d("ActivityFragment", "Notifikasi dijadwalkan pada: ${startTime.time}")
            startTime.add(Calendar.MINUTE, 30)
        }
    }

    private fun cancelScheduledNotification(schedule: ScheduleActivity) {
        val context = requireContext()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = try {
            sdf.parse(schedule.dateScheduled)
        } catch (e: Exception) {
            null
        }

        if (date == null) return

        val startTime = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 4)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endTime = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (startTime.before(endTime)) {
            val requestCode = (schedule.id ?: 0) + startTime.get(Calendar.MINUTE) + startTime.get(Calendar.HOUR_OF_DAY) * 100

            val intent = Intent(context, NotificationReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            Log.d("ActivityFragment", "Canceled notification id $requestCode at ${startTime.time}")

            startTime.add(Calendar.MINUTE, 30)
        }
    }

    private fun handleActivitySelection(activity: ScheduleActivity) {
        if (isCheckingProgress) return
        isCheckingProgress = true

        viewLifecycleOwner.lifecycleScope.launch {
            val hasProgress = activityPhysicalViewModel.checkIfUserHasProgressSuspend()
            Log.d("ActivityFragmentDebug", "Has Progress? $hasProgress")
            if (hasProgress) {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Aktivitas Sedang Berjalan")
                    setMessage("Hanya satu aktivitas yang bisa berlangsung dalam satu waktu. Selesaikan atau batalkan aktivitas sebelumnya terlebih dahulu.")
                    setPositiveButton("OK", null)
                    setOnDismissListener { isCheckingProgress = false }
                    create()
                    show()
                }
            } else {
                showConfirmationDialogAfterCheckingProgress(activity)
            }
        }
    }

    private fun showConfirmationDialogAfterCheckingProgress(activity: ScheduleActivity) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Konfirmasi")
            setMessage("Cuaca saat ini mendukung untuk aktivitas \"${activity.physicalActivityName}\". Apakah Anda ingin memulai aktivitas ini?")
            setPositiveButton("OK") { _, _ ->
                activityPhysicalViewModel.startScheduledActivity(activity)
                Toast.makeText(requireContext(), "Aktivitas ditambahkan ke progress", Toast.LENGTH_SHORT).show()
                cancelScheduledNotification(activity)
                //scheduledAdapter.removeItem(position)
                selectedSchedulePosition?.let { position ->
                    //scheduleGroupieAdapter.removeItem(position)
                }
                isCheckingProgress = false
            }
            setNegativeButton("Batal") { _, _ ->
                isCheckingProgress = false
            }
            setOnDismissListener {
                isCheckingProgress = false
            }
            create()
            show()
        }
    }

    // 2. Method untuk memulai timer
    private fun startTimeCounter(progressActivity: ProgressActivity) {
        // Stop existing timer if running
        stopTimeCounter()

        currentProgressActivity = progressActivity

        try {
            // Parse waktu mulai dari database
            val startTime = parseStartTime(progressActivity.dateStarted, progressActivity.startedAt)

            if (startTime != null) {
                // Gunakan waktu dari database sebagai referensi
                timerStartTime = startTime.time
                Log.d("ActivityFragment", "Using database start time: ${Date(timerStartTime)}")
            } else {
                // Fallback jika parsing gagal
                timerStartTime = System.currentTimeMillis()
                Log.w("ActivityFragment", "Failed to parse database time, using current time")
            }

            // Show time counter layout
            binding.llTimeCount.visibility = View.VISIBLE

            // Start timer that updates every second
            countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    updateTimeDisplay()
                }

                override fun onFinish() {
                    // This won't be called since we use Long.MAX_VALUE
                }
            }.start()

            isTimerRunning = true
            Log.d("ActivityFragment", "Timer started for activity: ${progressActivity.physicalActivityName}")

            // Initial display update
            updateTimeDisplay()

        } catch (e: Exception) {
            Log.e("ActivityFragment", "Error starting timer", e)
        }
    }

    // 3. Method untuk menghentikan timer
    private fun stopTimeCounter() {
        countDownTimer?.cancel()
        countDownTimer = null
        isTimerRunning = false
        currentProgressActivity = null
        timerStartTime = 0L

        // Immediately hide time counter UI
        binding.llTimeCount.visibility = View.GONE
        binding.tvTimeCountValue.text = "00:00:00"

        Log.d("ActivityFragment", "Timer stopped and UI hidden")
    }

    // 4. Method untuk update display waktu
    private fun updateTimeDisplay() {
        try {
            if (timerStartTime == 0L) {
                binding.tvTimeCountValue.text = "00:00:00"
                return
            }

            val currentTime = System.currentTimeMillis()
            val diffInMillis = currentTime - timerStartTime

            if (diffInMillis < 0) {
                binding.tvTimeCountValue.text = "00:00:00"
                return
            }

            // Hitung total detik yang sudah berlalu
            val totalSeconds = (diffInMillis / 1000).toInt()

            // Konversi ke jam, menit, detik untuk display
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            binding.tvTimeCountValue.text = timeString

            Log.d("ActivityFragment", "Time display updated: $timeString (total seconds: $totalSeconds, diff: ${diffInMillis}ms)")

        } catch (e: Exception) {
            Log.e("ActivityFragment", "Error updating time display", e)
            binding.tvTimeCountValue.text = "00:00:00"
        }
    }

    // Method untuk parse start time - SUDAH DIPERBAIKI
    private fun parseStartTime(dateStarted: String?, startedAt: String?): Date? {
        if (dateStarted == null || startedAt == null) {
            Log.w("ActivityFragment", "dateStarted or startedAt is null")
            return null
        }

        return try {
            Log.d("ActivityFragment", "Parsing time - dateStarted: $dateStarted, startedAt: $startedAt")

            val possibleFormats = listOf(
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "HH:mm:ss",
                "HH:mm"
            )

            var startDateTime: Date? = null

            // Jika startedAt sudah berisi tanggal lengkap
            if (startedAt.contains("-")) {
                Log.d("ActivityFragment", "startedAt contains date, parsing directly")
                for (format in possibleFormats) {
                    try {
                        startDateTime = SimpleDateFormat(format, Locale.getDefault()).parse(startedAt)
                        Log.d("ActivityFragment", "Successfully parsed with format: $format, result: $startDateTime")
                        break
                    } catch (e: Exception) {
                        Log.d("ActivityFragment", "Failed to parse with format: $format")
                        continue
                    }
                }
            } else {
                // Jika startedAt hanya berisi waktu, gabungkan dengan dateStarted
                val combinedDateTime = "$dateStarted $startedAt"
                Log.d("ActivityFragment", "Combined datetime: $combinedDateTime")

                for (format in possibleFormats) {
                    try {
                        startDateTime = SimpleDateFormat(format, Locale.getDefault()).parse(combinedDateTime)
                        Log.d("ActivityFragment", "Successfully parsed combined with format: $format, result: $startDateTime")
                        break
                    } catch (e: Exception) {
                        Log.d("ActivityFragment", "Failed to parse combined with format: $format")
                        continue
                    }
                }
            }

            if (startDateTime == null) {
                Log.e("ActivityFragment", "All parsing attempts failed")
            }

            startDateTime
        } catch (e: Exception) {
            Log.e("ActivityFragment", "Error parsing start time", e)
            null
        }
    }
}