package com.raihan.castfit.presentation.schedule

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings

import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.raihan.castfit.databinding.ActivityScheduleBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

/*class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding
    private val scheduleViewModel: ScheduleViewModel by viewModel()

    private var activityNames: List<String> = emptyList()
    private var selectedActivityName: String = ""
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("ScheduleActivity", "onCreate called")

        scheduleViewModel.loadInitialData()
        setupDatePicker()
        observeActivities()
        setupActivityDropdown()
        observeSaveResult()
        backHomePage()

        binding.btnSaveSchedule.setOnClickListener {
            Log.d("ScheduleActivity", "Save button clicked")
            saveSchedule()
        }
    }

    @Suppress("DEPRECATION")
    private fun backHomePage(){
        binding.btnBackHome.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        binding.etDateOfSchedule.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, day ->
                    // Simpan dalam format database (yyyy-MM-dd)
                    selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                    // Tampilkan dalam format display (dd/MM/yyyy)
                    val displayDate = String.format("%02d/%02d/%04d", day, month + 1, year)
                    binding.etDateOfSchedule.setText(displayDate)
                    Log.d("ScheduleActivity", "Date selected - Display: $displayDate, Database: $selectedDate")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Set minimal tanggal ke hari ini
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()

            datePickerDialog.show()
        }
    }


    private fun observeActivities() {
        scheduleViewModel.activities.observe(this) { list ->
            activityNames = list.map { it.name }
            Log.d("ScheduleActivity", "Activities loaded: ${activityNames.size} items")
            if (activityNames.isEmpty()) {
                Log.w("ScheduleActivity", "No activities loaded")
            }
        }
    }

    private fun setupActivityDropdown() {
        binding.etListActivity.setOnClickListener {
            if (activityNames.isNotEmpty()) {
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, activityNames)
                AlertDialog.Builder(this)
                    .setTitle("Pilih Aktivitas")
                    .setAdapter(adapter) { _, which ->
                        selectedActivityName = activityNames[which]
                        binding.etListActivity.setText(selectedActivityName)
                        Log.d("ScheduleActivity", "Activity selected: $selectedActivityName")
                    }
                    .show()
            } else {
                Toast.makeText(this, "Tunggu aktivitas dimuat...", Toast.LENGTH_SHORT).show()
                Log.w("ScheduleActivity", "Activities not loaded yet")
            }
        }
    }

    private fun saveSchedule() {
        Log.d("ScheduleActivity", "saveSchedule method called")

        // Ambil nilai dari field atau variabel yang sudah disimpan
        val activityName = if (selectedActivityName.isNotEmpty()) {
            selectedActivityName
        } else {
            binding.etListActivity.text.toString().trim()
        }

        val dateScheduled = if (selectedDate.isNotEmpty()) {
            selectedDate
        } else {
            // Jika selectedDate kosong, coba konversi dari display format
            val displayDate = binding.etDateOfSchedule.text.toString().trim()
            if (displayDate.isNotEmpty()) {
                convertDisplayDateToDbFormat(displayDate)
            } else {
                ""
            }
        }

        Log.d("ScheduleActivity", "Saving schedule - Activity: '$activityName', Date: '$dateScheduled'")

        // Validasi input
        if (activityName.isEmpty()) {
            Log.w("ScheduleActivity", "Activity name is empty")
            Toast.makeText(this, "Silakan pilih aktivitas fisik", Toast.LENGTH_SHORT).show()
            return
        }

        if (dateScheduled.isEmpty()) {
            Log.w("ScheduleActivity", "Date is empty")
            Toast.makeText(this, "Silakan pilih tanggal jadwal", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi tanggal tidak boleh di masa lalu
        if (!isDateValid(dateScheduled)) {
            Log.w("ScheduleActivity", "Date is not valid (in the past)")
            Toast.makeText(this, "Tanggal jadwal tidak boleh di masa lalu", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("ScheduleActivity", "All validations passed, calling ViewModel.saveSchedule")

        // Simpan jadwal
        scheduleViewModel.saveSchedule(
            selectedActivityName = activityName,
            selectedDate = dateScheduled,
            weatherStatus = "Cerah" // Default weather status
        )
    }

    private fun convertDisplayDateToDbFormat(displayDate: String): String {
        return try {
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = displayFormat.parse(displayDate)
            val result = dbFormat.format(date!!)
            Log.d("ScheduleActivity", "Date converted from '$displayDate' to '$result'")
            result
        } catch (e: Exception) {
            Log.e("ScheduleActivity", "Error converting date: $displayDate", e)
            ""
        }
    }

    private fun isDateValid(dateString: String): Boolean {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val selectedDate = format.parse(dateString)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val isValid = selectedDate != null && !selectedDate.before(today)
            Log.d("ScheduleActivity", "Date validation for '$dateString': $isValid")
            isValid
        } catch (e: Exception) {
            Log.e("ScheduleActivity", "Error validating date: $dateString", e)
            false
        }
    }

    private fun observeSaveResult() {
        scheduleViewModel.saveScheduleResult.observe(this) { success ->
            Log.d("ScheduleActivity", "Save result observed: $success")
            success?.let {
                if (it) {
                    Log.d("ScheduleActivity", "Schedule saved successfully")
                    Toast.makeText(this, "Jadwal berhasil disimpan", Toast.LENGTH_SHORT).show()
                    clearForm()
                    // Kembali ke halaman sebelumnya setelah berhasil menyimpan
                    finish()
                } else {
                    Log.e("ScheduleActivity", "Failed to save schedule")
                    Toast.makeText(this, "Gagal menyimpan jadwal", Toast.LENGTH_SHORT).show()
                }
                scheduleViewModel.resetSaveResult()
            }
        }

        scheduleViewModel.isLoading.observe(this) { isLoading ->
            binding.btnSaveSchedule.isEnabled = !isLoading
            Log.d("ScheduleActivity", "Loading state: $isLoading")
            // Anda bisa menambahkan progress bar jika diperlukan
        }
    }

    private fun clearForm() {
        binding.etListActivity.setText("")
        binding.etDateOfSchedule.setText("")
        selectedActivityName = ""
        selectedDate = ""
        Log.d("ScheduleActivity", "Form cleared")
    }
}*/

class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding
    private val scheduleViewModel: ScheduleViewModel by viewModel()

    private var activityNames: List<String> = emptyList()
    private var selectedActivityName: String = ""
    private var selectedDate: String = ""

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST = 1001
        private const val ALARM_PERMISSION_REQUEST = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("ScheduleActivity", "onCreate called")

        scheduleViewModel.loadInitialData()
        setupDatePicker()
        observeActivities()
        setupActivityDropdown()
        observeSaveResult()
        backHomePage()

        binding.btnSaveSchedule.setOnClickListener {
            Log.d("ScheduleActivity", "Save button clicked")
            saveSchedule()
        }
    }

    @Suppress("DEPRECATION")
    private fun backHomePage(){
        binding.btnBackHome.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        binding.etDateOfSchedule.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, day ->
                    // Simpan dalam format database (yyyy-MM-dd)
                    selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                    // Tampilkan dalam format display (dd/MM/yyyy)
                    val displayDate = String.format("%02d/%02d/%04d", day, month + 1, year)
                    binding.etDateOfSchedule.setText(displayDate)
                    Log.d("ScheduleActivity", "Date selected - Display: $displayDate, Database: $selectedDate")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Set minimal tanggal ke hari ini
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()

            datePickerDialog.show()
        }
    }

    private fun observeActivities() {
        scheduleViewModel.activities.observe(this) { list ->
            activityNames = list.map { it.name }
            Log.d("ScheduleActivity", "Activities loaded: ${activityNames.size} items")
            if (activityNames.isEmpty()) {
                Log.w("ScheduleActivity", "No activities loaded")
            }
        }
    }

    private fun setupActivityDropdown() {
        binding.etListActivity.setOnClickListener {
            if (activityNames.isNotEmpty()) {
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, activityNames)
                AlertDialog.Builder(this)
                    .setTitle("Pilih Aktivitas")
                    .setAdapter(adapter) { _, which ->
                        selectedActivityName = activityNames[which]
                        binding.etListActivity.setText(selectedActivityName)
                        Log.d("ScheduleActivity", "Activity selected: $selectedActivityName")
                    }
                    .show()
            } else {
                Toast.makeText(this, "Tunggu aktivitas dimuat...", Toast.LENGTH_SHORT).show()
                Log.w("ScheduleActivity", "Activities not loaded yet")
            }
        }
    }

    private fun saveSchedule() {
        Log.d("ScheduleActivity", "saveSchedule method called")

        // Ambil nilai dari field atau variabel yang sudah disimpan
        val activityName = if (selectedActivityName.isNotEmpty()) {
            selectedActivityName
        } else {
            binding.etListActivity.text.toString().trim()
        }

        val dateScheduled = if (selectedDate.isNotEmpty()) {
            selectedDate
        } else {
            // Jika selectedDate kosong, coba konversi dari display format
            val displayDate = binding.etDateOfSchedule.text.toString().trim()
            if (displayDate.isNotEmpty()) {
                convertDisplayDateToDbFormat(displayDate)
            } else {
                ""
            }
        }

        Log.d("ScheduleActivity", "Saving schedule - Activity: '$activityName', Date: '$dateScheduled'")

        // Validasi input
        if (activityName.isEmpty()) {
            Log.w("ScheduleActivity", "Activity name is empty")
            Toast.makeText(this, "Silakan pilih aktivitas fisik", Toast.LENGTH_SHORT).show()
            return
        }

        if (dateScheduled.isEmpty()) {
            Log.w("ScheduleActivity", "Date is empty")
            Toast.makeText(this, "Silakan pilih tanggal jadwal", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi tanggal tidak boleh di masa lalu
        if (!isDateValid(dateScheduled)) {
            Log.w("ScheduleActivity", "Date is not valid (in the past)")
            Toast.makeText(this, "Tanggal jadwal tidak boleh di masa lalu", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("ScheduleActivity", "All validations passed, calling ViewModel.saveSchedule")

        // Simpan jadwal
        scheduleViewModel.saveSchedule(
            selectedActivityName = activityName,
            selectedDate = dateScheduled,
            weatherStatus = "Cerah" // Default weather status
        )
    }

    private fun convertDisplayDateToDbFormat(displayDate: String): String {
        return try {
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = displayFormat.parse(displayDate)
            val result = dbFormat.format(date!!)
            Log.d("ScheduleActivity", "Date converted from '$displayDate' to '$result'")
            result
        } catch (e: Exception) {
            Log.e("ScheduleActivity", "Error converting date: $displayDate", e)
            ""
        }
    }

    private fun isDateValid(dateString: String): Boolean {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val selectedDate = format.parse(dateString)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val isValid = selectedDate != null && !selectedDate.before(today)
            Log.d("ScheduleActivity", "Date validation for '$dateString': $isValid")
            isValid
        } catch (e: Exception) {
            Log.e("ScheduleActivity", "Error validating date: $dateString", e)
            false
        }
    }

    private fun observeSaveResult() {
        scheduleViewModel.saveScheduleResult.observe(this) { success ->
            Log.d("ScheduleActivity", "Save result observed: $success")
            success?.let {
                if (it) {
                    Log.d("ScheduleActivity", "Schedule saved successfully")
                    Toast.makeText(this, "Jadwal berhasil disimpan", Toast.LENGTH_SHORT).show()
                    clearForm()

                    // Request permissions setelah jadwal berhasil disimpan
                    requestNotificationAndAlarmPermissions()

                } else {
                    Log.e("ScheduleActivity", "Failed to save schedule")
                    Toast.makeText(this, "Gagal menyimpan jadwal", Toast.LENGTH_SHORT).show()
                }
                scheduleViewModel.resetSaveResult()
            }
        }

        scheduleViewModel.isLoading.observe(this) { isLoading ->
            binding.btnSaveSchedule.isEnabled = !isLoading
            Log.d("ScheduleActivity", "Loading state: $isLoading")
            // Anda bisa menambahkan progress bar jika diperlukan
        }
    }

    private fun requestNotificationAndAlarmPermissions() {
        // Request notification permission untuk Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST
                )
                return // Akan dilanjutkan di onRequestPermissionsResult
            }
        }

        // Jika notification permission sudah ada atau tidak diperlukan, lanjut ke alarm permission
        requestAlarmPermission()
    }

    private fun requestAlarmPermission() {
        // Request exact alarm permission untuk Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                AlertDialog.Builder(this)
                    .setTitle("Izin Alarm Diperlukan")
                    .setMessage("Untuk mengirimkan pengingat jadwal aktivitas, aplikasi memerlukan izin untuk menjadwalkan alarm yang tepat. Silakan berikan izin pada pengaturan yang akan dibuka.")
                    .setPositiveButton("Buka Pengaturan") { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        try {
                            //startActivityForResult(intent, ALARM_PERMISSION_REQUEST)
                            startActivity(intent) // tidak perlu startActivityForResult
                            Toast.makeText(this, "Aktifkan izin alarm secara manual di pengaturan", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Log.e("ScheduleActivity", "Error opening alarm settings", e)
                            Toast.makeText(this, "Tidak dapat membuka pengaturan alarm", Toast.LENGTH_SHORT).show()
                            finishScheduleCreation()
                        }
                    }
                    .setNegativeButton("Lewati") { _, _ ->
                        Toast.makeText(this, "Pengingat mungkin tidak berfungsi dengan baik", Toast.LENGTH_LONG).show()
                        finishScheduleCreation()
                    }
                    .show()
            } else {
                // Permission sudah ada, selesai
                finishScheduleCreation()
            }
        } else {
            // Android < 12, tidak perlu permission khusus
            finishScheduleCreation()
        }
    }

    private fun finishScheduleCreation() {
        // Kembali ke halaman sebelumnya setelah semua permission selesai
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("ScheduleActivity", "Notification permission granted")
                    // Lanjut ke alarm permission
                    requestAlarmPermission()
                } else {
                    Log.w("ScheduleActivity", "Notification permission denied")
                    Toast.makeText(this, "Izin notifikasi diperlukan untuk pengingat jadwal", Toast.LENGTH_LONG).show()
                    // Tetap lanjut ke alarm permission
                    requestAlarmPermission()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ALARM_PERMISSION_REQUEST -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (alarmManager.canScheduleExactAlarms()) {
                        Log.d("ScheduleActivity", "Exact alarm permission granted")
                        Toast.makeText(this, "Pengingat akan bekerja dengan baik", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w("ScheduleActivity", "Exact alarm permission not granted")
                        Toast.makeText(this, "Pengingat mungkin tidak berfungsi dengan baik", Toast.LENGTH_LONG).show()
                    }
                }
                finishScheduleCreation()
            }
        }
    }

    private fun clearForm() {
        binding.etListActivity.setText("")
        binding.etDateOfSchedule.setText("")
        selectedActivityName = ""
        selectedDate = ""
        Log.d("ScheduleActivity", "Form cleared")
    }
}
