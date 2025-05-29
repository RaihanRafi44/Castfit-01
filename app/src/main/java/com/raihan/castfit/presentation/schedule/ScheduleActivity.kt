package com.raihan.castfit.presentation.schedule

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

        setupDatePicker()
        observeActivities()
        setupActivityDropdown()
        observeSaveResult()
        backHomePage()
        binding.btnSaveSchedule.setOnClickListener {
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
            DatePickerDialog(this, { _, year, month, day ->
                val selectedDate = String.format("%02d/%02d/%04d", day, month + 1, year)
                binding.etDateOfSchedule.setText(selectedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun observeActivities() {
        scheduleViewModel.activities.observe(this) { list ->
            activityNames = list.map { it.name }
        }
    }

    private fun setupActivityDropdown() {
        binding.etListActivity.setOnClickListener {
            if (activityNames.isNotEmpty()) {
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, activityNames)
                AlertDialog.Builder(this)
                    .setTitle("Pilih Aktivitas")
                    .setAdapter(adapter) { _, which ->
                        binding.etListActivity.setText(activityNames[which])
                    }
                    .show()
            }
        }
    }

    private fun saveSchedule() {
        val activityName = selectedActivityName.ifEmpty {
            binding.etListActivity.text.toString().trim()
        }
        val dateScheduled = selectedDate.ifEmpty {
            // Jika selectedDate kosong, coba konversi dari display format
            val displayDate = binding.etDateOfSchedule.text.toString().trim()
            if (displayDate.isNotEmpty()) {
                convertDisplayDateToDbFormat(displayDate)
            } else {
                ""
            }
        }

        // Validasi input
        if (activityName.isEmpty()) {
            Toast.makeText(this, "Silakan pilih aktivitas fisik", Toast.LENGTH_SHORT).show()
            return
        }

        if (dateScheduled.isEmpty()) {
            Toast.makeText(this, "Silakan pilih tanggal jadwal", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi tanggal tidak boleh di masa lalu
        if (!isDateValid(dateScheduled)) {
            Toast.makeText(this, "Tanggal jadwal tidak boleh di masa lalu", Toast.LENGTH_SHORT).show()
            return
        }

        // Simpan jadwal
        scheduleViewModel.saveSchedule(
            selectedActivityName = activityName,
            selectedDate = dateScheduled,
            weatherStatus = "" // Default weather status
        )
    }

    private fun convertDisplayDateToDbFormat(displayDate: String): String {
        return try {
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = displayFormat.parse(displayDate)
            dbFormat.format(date!!)
        } catch (e: Exception) {
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

            selectedDate != null && !selectedDate.before(today)
        } catch (e: Exception) {
            false
        }
    }

    private fun observeSaveResult() {
        scheduleViewModel.saveScheduleResult.observe(this) { success ->
            success?.let {
                if (it) {
                    Toast.makeText(this, "Jadwal berhasil disimpan", Toast.LENGTH_SHORT).show()
                    clearForm()
                } else {
                    Toast.makeText(this, "Gagal menyimpan jadwal", Toast.LENGTH_SHORT).show()
                }
                scheduleViewModel.resetSaveResult()
            }
        }

        scheduleViewModel.isLoading.observe(this) { isLoading ->
            binding.btnSaveSchedule.isEnabled = !isLoading
            // Anda bisa menambahkan progress bar jika diperlukan
        }
    }

    private fun clearForm() {
        binding.etListActivity.setText("")
        binding.etDateOfSchedule.setText("")
        selectedActivityName = ""
        selectedDate = ""
    }
}*/

class ScheduleActivity : AppCompatActivity() {

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

    /*private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        binding.etDateOfSchedule.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                // Simpan dalam format database (yyyy-MM-dd)
                selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                // Tampilkan dalam format display (dd/MM/yyyy)
                val displayDate = String.format("%02d/%02d/%04d", day, month + 1, year)
                binding.etDateOfSchedule.setText(displayDate)
                Log.d("ScheduleActivity", "Date selected - Display: $displayDate, Database: $selectedDate")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }*/

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
}
