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

        // Check if user is logged in first
        if (recommendationViewModel.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_LONG).show()
            finish()
            return
        }

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

    /*private fun observeViewModel() {
        recommendationViewModel.indoorActivities.observe(this) { indoorAdapter.setData(it) }
        recommendationViewModel.outdoorActivities.observe(this) { outdoorAdapter.setData(it) }
    }*/

    private fun observeViewModel() {
        recommendationViewModel.indoorActivities.observe(this) {
            indoorAdapter.setData(it)
        }

        recommendationViewModel.outdoorActivities.observe(this) { outdoorList ->
            outdoorAdapter.setData(outdoorList)

            if (outdoorList.isNullOrEmpty()) {
                binding.rvOutdoorList.visibility = android.view.View.GONE
                binding.tvOutdoorEmpty.visibility = android.view.View.VISIBLE
            } else {
                binding.rvOutdoorList.visibility = android.view.View.VISIBLE
                binding.tvOutdoorEmpty.visibility = android.view.View.GONE
            }
        }
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