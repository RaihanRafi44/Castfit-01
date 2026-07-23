package com.raihan.castfit.presentation.recommendation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.raihan.castfit.R
import com.raihan.castfit.data.model.PhysicalActivity
import com.raihan.castfit.databinding.ActivityRecommendationBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecommendationActivity : AppCompatActivity() {

    private val binding: ActivityRecommendationBinding by lazy {
        ActivityRecommendationBinding.inflate(layoutInflater)
    }

    private val recommendationViewModel: RecommendationViewModel by viewModel()

    private lateinit var indoorAdapter: RecommendationIndoorAdapter
    private lateinit var outdoorAdapter: RecommendationOutdoorAdapter

    //private var isDialogShown = false
    // Menandai apakah sedang memeriksa progress aktivitas
    private var isCheckingProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Cek apakah user sudah login, jika belum keluar dari activity
        if (recommendationViewModel.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Inisialisasi adapter dan listener pemilihan aktivitas
        indoorAdapter = RecommendationIndoorAdapter { activity ->
            handleActivitySelection(activity)
        }

        outdoorAdapter = RecommendationOutdoorAdapter { activity ->
            handleActivitySelection(activity)
        }

        proceedList()
        observeViewModel()

        // Ambil kondisi cuaca dari intent dan load aktivitas yang sesuai
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

    // Mengamati perubahan data dari ViewModel dan menyesuaikan UI
    private fun observeViewModel() {
        recommendationViewModel.indoorActivities.observe(this) {
            indoorAdapter.setData(it)
        }

        // Observe outdoor list dan data lengkap sekaligus
        recommendationViewModel.outdoorActivities.observe(this) { outdoorList ->
            outdoorAdapter.setData(outdoorList)

            val userAge = recommendationViewModel.userAge.value
            val allActivities = recommendationViewModel.allActivities.value

            if (userAge != null && allActivities != null) {
                val outdoorAll = allActivities.filter { it.type.equals("Outdoor", ignoreCase = true) }

                val isOutdoorCapable = outdoorAll.any { activity ->
                    userAge >= activity.minAge
                }

                when {
                    !isOutdoorCapable -> {
                        binding.rvOutdoorList.isVisible = false
                        binding.tvOutdoorEmpty.isVisible = false
                        binding.tvOutdoorAgeNotCapable.isVisible = true
                    }
                    outdoorList.isNullOrEmpty() -> {
                        binding.rvOutdoorList.isVisible = false
                        binding.tvOutdoorEmpty.isVisible = true
                        binding.tvOutdoorAgeNotCapable.isVisible = false
                    }
                    else -> {
                        binding.rvOutdoorList.isVisible = true
                        binding.tvOutdoorEmpty.isVisible = false
                        binding.tvOutdoorAgeNotCapable.isVisible = false
                    }
                }
            }
        }

        recommendationViewModel.allActivities.observe(this) { allActivities ->
            val userAge = recommendationViewModel.userAge.value
            val indoorAll = allActivities.filter { it.type.equals("Indoor", ignoreCase = true) }

            val isIndoorCapable = indoorAll.any { activity ->
                userAge != null && userAge >= activity.minAge
            }

            binding.tvIndoorAgeNotCapable.visibility = if (!isIndoorCapable) View.VISIBLE else View.GONE
            binding.rvIndoorList.visibility = if (!isIndoorCapable) View.GONE else View.VISIBLE

        }
    }



    @Suppress("DEPRECATION")
    private fun backHomePage(){
        binding.btnBackRecommendation.setOnClickListener {
            onBackPressed()
        }
    }

    // Menampilkan dialog konfirmasi modern sebelum memulai aktivitas
    private fun showConfirmationDialog(activity: PhysicalActivity) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialog_confirmation, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Bind view dari custom layout
        val ivIcon = dialogView.findViewById<ImageView>(R.id.iv_dialog_icon)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_dialog_message)
        val btnPositive = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_positive)
        val btnNegative = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_negative)

        // Load ikon aktivitas secara dinamis menggunakan physicalImg
        ivIcon.load(activity.physicalImg) {
            crossfade(true)
            placeholder(R.drawable.ic_activity)
            error(R.drawable.ic_activity)
        }

        tvTitle.text = "Mulai Aktivitas?"
        tvMessage.text = "Apakah Anda ingin memulai aktivitas \"${activity.name}\" sekarang?"

        btnPositive.setOnClickListener {
            recommendationViewModel.addToProgress(activity)
            Toast.makeText(this, "Aktivitas ditambahkan ke progress", Toast.LENGTH_SHORT).show()
            isCheckingProgress = false
            dialog.dismiss()
            finish()
        }

        btnNegative.setOnClickListener {
            isCheckingProgress = false
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            isCheckingProgress = false
        }

        dialog.show()
    }


    // Menangani pemilihan aktivitas, dan cek apakah user sudah punya aktivitas berjalan
    private fun handleActivitySelection(activity: PhysicalActivity) {
        if (isCheckingProgress) return
        isCheckingProgress = true

        lifecycleScope.launch {
            val hasProgress = recommendationViewModel.checkIfUserHasProgressSuspend()
            Log.d("RecommendationDebug", "Has Progress? $hasProgress")
            if (hasProgress) {
                MaterialAlertDialogBuilder(this@RecommendationActivity)
                    .setTitle("Aktivitas Sedang Berjalan")
                    .setMessage("Hanya satu aktivitas yang bisa berlangsung dalam satu waktu. Selesaikan atau batalkan aktivitas sebelumnya terlebih dahulu.")
                    .setPositiveButton("OK") { _, _ -> isCheckingProgress = false }
                    .setOnDismissListener { isCheckingProgress = false }
                    .show()
            } else {
                showConfirmationDialog(activity)
            }
        }
    }

}