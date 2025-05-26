package com.raihan.castfit.presentation.schedule

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.raihan.castfit.databinding.ActivityScheduleBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding
    private val scheduleViewModel: ScheduleViewModel by viewModel()

    private var activityNames: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDatePicker()
        observeActivities()
        setupActivityDropdown()
        //binding.btnBackHome.setOnClickListener { finish() }
        backHomePage()
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
}
