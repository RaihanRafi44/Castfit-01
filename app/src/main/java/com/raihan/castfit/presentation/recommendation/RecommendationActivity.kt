package com.raihan.castfit.presentation.recommendation

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.raihan.castfit.data.model.PhysicalActivity
import com.raihan.castfit.databinding.ActivityRecommendationBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecommendationActivity : AppCompatActivity() {

    private val binding: ActivityRecommendationBinding by lazy {
        ActivityRecommendationBinding.inflate(layoutInflater)
    }

    private val recommendationViewModel: RecommendationViewModel by viewModel()

    private lateinit var indoorAdapter: RecommendationIndoorAdapter
    private lateinit var outdoorAdapter: RecommendationOutdoorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        indoorAdapter = RecommendationIndoorAdapter { activity ->
            showConfirmationDialog(activity)
        }

        outdoorAdapter = RecommendationOutdoorAdapter { activity ->
            showConfirmationDialog(activity)
        }
        proceedList()
        observeViewModel()
        //recommendationViewModel.loadActivities()
        val weatherCondition = intent.getStringExtra("weatherCondition") ?: ""
        Log.d("RecommendationActivity", "Received weather condition: $weatherCondition")
        recommendationViewModel.loadActivitiesBasedOnWeather(weatherCondition)
        backHomePage()
    }

    private fun proceedList(){
        binding.rvIndoorList.apply {
            layoutManager = LinearLayoutManager(this@RecommendationActivity)
            adapter = indoorAdapter
        }

        binding.rvOutdoorList.apply {
            layoutManager = LinearLayoutManager(this@RecommendationActivity)
            adapter = outdoorAdapter
        }
    }

    private fun observeViewModel() {
        recommendationViewModel.indoorActivities.observe(this) { indoorAdapter.setData(it) }
        recommendationViewModel.outdoorActivities.observe(this) { outdoorAdapter.setData(it) }
    }

    @Suppress("DEPRECATION")
    private fun backHomePage(){
        binding.btnBackRecommendation.setOnClickListener {
            onBackPressed()
        }
    }

    private fun showConfirmationDialog(activity: PhysicalActivity) {
        AlertDialog.Builder(this).apply {
            setTitle("Konfirmasi")
            setMessage("Apakah Anda ingin memulai aktivitas \"${activity.name}\"?")
            setPositiveButton("OK") { _, _ ->
                recommendationViewModel.addToProgress(activity)
                Toast.makeText(this@RecommendationActivity, "Aktivitas ditambahkan ke progress", Toast.LENGTH_SHORT).show()
                Log.d("ActivityLog", "Aktivitas '${activity.name}' berhasil ditambahkan ke halaman Activity.")
                finish() // Kembali ke halaman utama
            }
            setNegativeButton("Batal", null)
            create()
            show()
        }
    }

}