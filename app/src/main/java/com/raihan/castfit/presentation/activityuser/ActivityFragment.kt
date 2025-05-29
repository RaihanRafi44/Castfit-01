package com.raihan.castfit.presentation.activityuser

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.raihan.castfit.R
import com.raihan.castfit.data.model.HistoryActivity
import com.raihan.castfit.data.model.ProgressActivity
import com.raihan.castfit.data.model.ScheduleActivity
import com.raihan.castfit.databinding.FragmentActivityBinding
import com.raihan.castfit.presentation.home.HomeViewModel
import com.raihan.castfit.utils.proceedWhen
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ActivityFragment : Fragment() {

    private lateinit var binding: FragmentActivityBinding
    private val activityPhysicalViewModel: ActivityViewModel by viewModel()
    private val homeViewModel: HomeViewModel by activityViewModel()
    //private val homeViewModel: HomeViewModel by viewModel()
    private var selectedSchedulePosition: Int? = null

    private val scheduledAdapter = ScheduleAdapter(
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

    private val historyAdapter = HistoryActivityAdapter { history, position ->
        showDeleteHistoryDialog(history, position)
    }

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
                    binding.rvScheduledList.isVisible = true
                    it.payload?.let { list ->
                        Log.d("ActivityFragment", "Data schedule size: ${list.size}")
                        scheduledAdapter.setData(list)
                    } ?: Log.d("ActivityFragment", "Schedule payload null")
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
                    binding.rvOnProgressList.isVisible = true
                    it.payload?.let { list ->
                        Log.d("ActivityFragment", "Data on progress size: ${list.size}")
                        progressAdapter.setData(list)
                    } ?: Log.d("ActivityFragment", "Progress payload null")
                },
                doOnError = {
                    Log.e("ActivityFragment", "Error loading progress: ${it.exception}")
                    binding.rvOnProgressList.isVisible = false
                },
                doOnLoading = {
                    Log.d("ActivityFragment", "Loading on progress data...")
                }
            )
        }

        // Observe history data
        activityPhysicalViewModel.getAllHistory().observe(viewLifecycleOwner) { result ->
            result.proceedWhen(
                doOnSuccess = {
                    binding.rvHistoryList.isVisible = true
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
        }
    }

    private fun setupRecyclerViews() {
        // Setup scheduled RecyclerView
        binding.rvScheduledList.adapter = scheduledAdapter
        binding.rvScheduledList.layoutManager = LinearLayoutManager(requireContext())

        // Setup progress RecyclerView
        binding.rvOnProgressList.adapter = progressAdapter
        binding.rvOnProgressList.layoutManager = LinearLayoutManager(requireContext())

        // Setup history RecyclerView
        binding.rvHistoryList.adapter = historyAdapter
        binding.rvHistoryList.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showDeleteScheduledDialog(schedule: ScheduleActivity, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Jadwal?")
            .setMessage("Apakah kamu yakin ingin menghapus jadwal aktivitas '${schedule.physicalActivityName}' pada tanggal ${schedule.dateScheduled}?")
            .setPositiveButton("Ya") { _, _ ->
                activityPhysicalViewModel.removeSchedule(schedule)
                scheduledAdapter.removeItem(position)
                Log.d("ActivityLog", "Jadwal '${schedule.physicalActivityName}' akan dihapus dari scheduled list.")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    /*private fun showContinueScheduledDialog(schedule: ScheduleActivity, position: Int) {
        // TODO: Implement continue scheduled functionality
        Toast.makeText(requireContext(), "Fitur lanjutkan jadwal akan diimplementasikan nanti", Toast.LENGTH_SHORT).show()
    }*/

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
                    // Weather is suitable - show confirmation dialog
                    AlertDialog.Builder(requireContext())
                        .setTitle(weatherCheck.confirmationTitle)
                        .setMessage(weatherCheck.message)
                        .setPositiveButton("Ya, Mulai") { _, _ ->
                            activityPhysicalViewModel.startScheduledActivity(weatherCheck.schedule)
                            //scheduledAdapter.removeItem(position)
                            selectedSchedulePosition?.let { position ->
                                scheduledAdapter.removeItem(position)
                            }
                        }
                        .setNegativeButton("Nanti Saja", null)
                        .show()
                } else {
                    // Weather is not suitable - show warning dialog with options
                    AlertDialog.Builder(requireContext())
                        .setTitle(weatherCheck.confirmationTitle)
                        .setMessage(weatherCheck.message)
                        .setPositiveButton("Hapus Jadwal") { _, _ ->
                            activityPhysicalViewModel.removeSchedule(weatherCheck.schedule)
                            selectedSchedulePosition?.let { position ->
                                scheduledAdapter.removeItem(position)
                            }
                        }
                        .setNegativeButton("Nanti Saja", null)
                        .show()
                }
                activityPhysicalViewModel.resetWeatherCheckResult()
                //selectedSchedulePosition = null
            }
        }
    }

    private fun observeStartScheduledActivityResult() {
        activityPhysicalViewModel.startScheduledActivityResult.observe(viewLifecycleOwner) { result ->
            result?.let { success ->
                if (success) {
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
                activityPhysicalViewModel.removeProgress(activity)
                progressAdapter.removeItem(position)
                Log.d("ActivityLog", "Aktivitas '${activity.physicalActivityName}' akan dihapus dari halaman Activity.")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun showFinishDialog(activity: ProgressActivity, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Selesaikan Aktivitas?")
            .setMessage("Apakah kamu yakin ingin menyelesaikan aktivitas '${activity.physicalActivityName}'? Aktivitas akan dipindahkan ke riwayat.")
            .setPositiveButton("Ya") { _, _ ->
                activityPhysicalViewModel.finishActivity(activity)
                progressAdapter.removeItem(position) //Hapus UI list dari progress
                Log.d("ActivityLog", "Aktivitas '${activity.physicalActivityName}' akan dipindahkan ke history.")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun showDeleteHistoryDialog(history: HistoryActivity, position: Int) {
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
}