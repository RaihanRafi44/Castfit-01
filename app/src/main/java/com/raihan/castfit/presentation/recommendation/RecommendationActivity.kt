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
import com.raihan.castfit.databinding.LayoutDialogAlertBinding
import com.raihan.castfit.databinding.LayoutDialogConfirmationBinding
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

    private fun showLoading(isLoading: Boolean) {
        binding.layoutShimmer.root.isVisible = isLoading
        binding.nsvContent.isVisible = !isLoading
        if (isLoading) {
            binding.layoutShimmer.shimmerViewContainer.startShimmer()
        } else {
            binding.layoutShimmer.shimmerViewContainer.stopShimmer()
        }
    }

    // Mengamati perubahan data dari ViewModel dan menyesuaikan UI
    private fun observeViewModel() {
        recommendationViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        recommendationViewModel.indoorActivities.observe(this) {
            indoorAdapter.setData(it)
            checkEmptyState()
        }

        // Observe outdoor list dan data lengkap sekaligus
        recommendationViewModel.outdoorActivities.observe(this) { outdoorList ->
            outdoorAdapter.setData(outdoorList)
            checkEmptyState()

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
            checkEmptyState()
        }
    }

    private fun checkEmptyState() {
        val indoorList = recommendationViewModel.indoorActivities.value ?: emptyList()
        val outdoorList = recommendationViewModel.outdoorActivities.value ?: emptyList()
        val isLoading = recommendationViewModel.isLoading.value ?: false

        if (isLoading) return

        val isEmpty = indoorList.isEmpty() && outdoorList.isEmpty()

        binding.llRecommendationEmptyState.isVisible = isEmpty
        
        // Jika benar-benar kosong total (baik indoor maupun outdoor)
        if (isEmpty) {
            binding.incOutdoorTitle.root.isVisible = false
            binding.flOutdoor.isVisible = false
            binding.incIndoorTitle.root.isVisible = false
            binding.flIndoor.isVisible = false
        } else {
            // Jika ada salah satu yang isi, pastikan section-section tetap terlihat
            // agar logika masing-masing di observeViewModel bisa berjalan
            binding.incOutdoorTitle.root.isVisible = true
            binding.flOutdoor.isVisible = true
            binding.incIndoorTitle.root.isVisible = true
            binding.flIndoor.isVisible = true
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
        val dialogBinding = LayoutDialogConfirmationBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        // Load ikon aktivitas secara dinamis menggunakan physicalImg
        dialogBinding.ivDialogIcon.load(activity.physicalImg) {
            crossfade(true)
            placeholder(R.drawable.ic_activity)
            error(R.drawable.ic_activity)
        }
        
        dialogBinding.tvDialogTitle.text = "Mulai Aktivitas?"
        dialogBinding.tvDialogMessage.text = "Apakah Anda ingin memulai aktivitas \"${activity.name}\" sekarang?"

        dialogBinding.btnDialogPositive.setOnClickListener {
            recommendationViewModel.addToProgress(activity)
            Toast.makeText(this, "Aktivitas ditambahkan ke progress", Toast.LENGTH_SHORT).show()
            isCheckingProgress = false
            dialog.dismiss()
            finish()
        }

        dialogBinding.btnDialogNegative.setOnClickListener {
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
                val dialogBinding = LayoutDialogAlertBinding.inflate(layoutInflater)
                val dialog = MaterialAlertDialogBuilder(this@RecommendationActivity)
                    .setView(dialogBinding.root)
                    .setCancelable(true)
                    .create()

                dialogBinding.tvAlertTitle.text = "Aktivitas Sedang Berjalan"
                dialogBinding.tvAlertMessage.text = "Hanya satu aktivitas yang bisa berlangsung dalam satu waktu. Selesaikan atau batalkan aktivitas sebelumnya terlebih dahulu."
                
                dialogBinding.btnAlertOk.setOnClickListener {
                    isCheckingProgress = false
                    dialog.dismiss()
                }

                dialog.setOnDismissListener { isCheckingProgress = false }
                dialog.show()
            } else {
                showConfirmationDialog(activity)
            }
        }
    }

}