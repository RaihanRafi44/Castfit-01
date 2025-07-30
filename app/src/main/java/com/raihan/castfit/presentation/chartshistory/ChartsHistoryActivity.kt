package com.raihan.castfit.presentation.chartshistory

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.aachartmodel.aainfographics.aachartcreator.*
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AADataLabels
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAScrollablePlotArea
import com.raihan.castfit.R
import com.raihan.castfit.data.model.HistoryActivity
import com.raihan.castfit.data.repository.HistoryActivityRepository
import com.raihan.castfit.databinding.ActivityChartsHistoryBinding
import com.raihan.castfit.databinding.ActivityRecommendationBinding
import com.raihan.castfit.utils.ResultWrapper
import com.raihan.castfit.utils.proceedWhen
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChartsHistoryActivity : AppCompatActivity() {

    private val binding: ActivityChartsHistoryBinding by lazy {
        ActivityChartsHistoryBinding.inflate(layoutInflater)
    }

    private val chartsViewModel: ChartsHistoryViewModel by viewModel()
    private val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val aaChartView = binding.AAChartView1

        observeHistoryData(aaChartView)
        backHomePage()
    }

    @Suppress("DEPRECATION")
    private fun backHomePage(){
        binding.btnBackChartsActivity.setOnClickListener {
            onBackPressed()
        }
    }

    private fun observeHistoryData(aaChartView: AAChartView) {
        chartsViewModel.getAllHistory().observe(this) { result ->
            result.proceedWhen(
                doOnSuccess = {
                    it.payload?.let { historyList ->
                        hideLoading()
                        showChart()
                        setupChart(aaChartView, historyList)
                    } ?: setupEmptyChart(aaChartView)
                },
                doOnError = {
                    hideLoading()
                    Log.e("ChartsHistory", "Error loading history data", it.exception)
                    showEmptyState()
                },
                doOnLoading = {
                    showLoading()
                    Log.d("ChartsHistory", "Loading history data...")
                },
                doOnEmpty = {
                    showEmptyState()
                    hideLoading()
                    Log.d("ChartsHistory", "History data is empty")
                }
            )
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.horizontalScrollCharts.visibility = View.GONE
        binding.llEmptyState.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showChart() {
        binding.horizontalScrollCharts.visibility = View.VISIBLE
        binding.llEmptyState.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.horizontalScrollCharts.visibility = View.GONE
        binding.llEmptyState.visibility = View.VISIBLE
    }

    // Menggambar grafik berdasarkan data riwayat yang tersedia
    private fun setupChart(aaChartView: AAChartView, historyList: List<HistoryActivity>) {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val firstLoginDate = sharedPref.getString("first_login_date", null)

        val chartData = if (firstLoginDate != null) {
            processChartData(historyList, firstLoginDate)
        } else {
            processChartData(historyList)
        }


        val aaChartModel = AAChartModel()
            .chartType(AAChartType.Column)
            .title("Riwayat Aktivitas Fisik Pengguna Per 7 Hari")
            .subtitle("Data Aktivitas Fisik")
            .categories(chartData.categories.toTypedArray())
            .dataLabelsEnabled(true)
            .yAxisTitle("Durasi (Menit)")
            .xAxisVisible(true)
            .stacking(AAChartStackingType.Normal)
            .tooltipValueSuffix(" menit")
            .series(arrayOf(
                AASeriesElement()
                    .name("Indoor")
                    .data(chartData.indoorData.toTypedArray())
                    .color("#4CAF50"), // Hijau

                AASeriesElement()
                    .name("Outdoor")
                    .data(chartData.outdoorData.toTypedArray())
                    .color("#2196F3") // Biru
            ))

        val aaOptions = aaChartModel.aa_toAAOptions()

        aaOptions.plotOptions?.column?.dataLabels = AADataLabels()
            .enabled(true)
            .formatter("""
            function () {
                return this.y + ' menit';
            }
        """.trimIndent())

        aaOptions.chart?.scrollablePlotArea(
            AAScrollablePlotArea()
                .minWidth(800)
                .scrollPositionX(1f)
        )

        aaChartView.aa_drawChartWithChartOptions(aaOptions)
    }


    // Menyiapkan grafik kosong jika data riwayat tidak tersedia
    private fun setupEmptyChart(aaChartView: AAChartView) {
        //val emptyData = getEmptyWeekData()
        val firstLoginDate = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getString("first_login_date", fullDateFormat.format(Date())) ?: fullDateFormat.format(Date())

        val emptyData = getEmptyWeekData(firstLoginDate)


        val aaChartModel = AAChartModel()
            .chartType(AAChartType.Column)
            .title("Riwayat Aktivitas Fisik Pengguna Per 7 Hari")
            .subtitle("Data Aktivitas Fisik")
            .categories(emptyData.categories.toTypedArray())
            .dataLabelsEnabled(true)
            .yAxisTitle("Durasi (Menit)")
            .xAxisVisible(true)
            .stacking(AAChartStackingType.Normal)
            .series(arrayOf(
                AASeriesElement()
                    .name("Indoor")
                    .data(emptyData.indoorData.toTypedArray())
                    .color("#4CAF50"),

                AASeriesElement()
                    .name("Outdoor")
                    .data(emptyData.outdoorData.toTypedArray())
                    .color("#2196F3")
            ))

        val aaOptions = aaChartModel.aa_toAAOptions()
        aaOptions.chart?.scrollablePlotArea(
            AAScrollablePlotArea()
                .minWidth(600)
                .scrollPositionX(1f)
        )

        aaChartView.aa_drawChartWithChartOptions(aaOptions)
    }

    // Mengelola data grafik dari riwayat aktivitas, memproses indoor & outdoor berdasarkan tanggal
    private fun processChartData(
        historyList: List<HistoryActivity>,
        firstLoginDate: String? = null
    ): ChartData {
        val dateRange = if (firstLoginDate != null) {
            chartsViewModel.getDateRangeFromFirstLogin(firstLoginDate)
        } else {
            chartsViewModel.getLast7DaysRange()
        }
        val categories = mutableListOf<String>()
        val indoorData = mutableListOf<Any>()
        val outdoorData = mutableListOf<Any>()

        // Group activities by date
        val activitiesByDate = historyList.groupBy { it.dateEnded }

        for (date in dateRange) {
            val dateString = fullDateFormat.format(date)
            val activitiesOnDate = activitiesByDate[dateString] ?: emptyList()

            // Tambahkan tanggal yang diformat ke kategori
            categories.add(chartsViewModel.formatDateForChart(date))

            // Hitung total durasi untuk aktivitas di dalam dan luar ruangan
            var indoorDurationMinutes = 0
            var outdoorDurationMinutes = 0

            for (activity in activitiesOnDate) {

                val durationMinutes = activity.duration ?: 0
                val activityType = chartsViewModel.getActivityType(activity.physicalActivityName)

                Log.d("ChartData", "Activity: ${activity.physicalActivityName}, Duration: $durationMinutes minutes, Type: $activityType")

                when (activityType) {
                    "Indoor" -> indoorDurationMinutes += durationMinutes
                    "Outdoor" -> outdoorDurationMinutes += durationMinutes
                }
            }

            Log.d("ChartData", "Date: $dateString, Indoor: $indoorDurationMinutes min, Outdoor: $outdoorDurationMinutes min")

            // Menangani logika tampilan durasi
            indoorData.add(when {
                indoorDurationMinutes == 0 -> 0
                //indoorDurationMinutes < 1 -> 0.5 // Show as 0.5 for chart visualization for very small durations
                else -> indoorDurationMinutes
            })

            outdoorData.add(when {
                outdoorDurationMinutes == 0 -> 0
                //outdoorDurationMinutes < 1 -> 0.5 // Show as 0.5 for chart visualization for very small durations
                else -> outdoorDurationMinutes
            })
        }

        return ChartData(categories, indoorData, outdoorData)
    }

    // Menghasilkan data kosong untuk 7 hari terakhir sebagai fallback
    private fun getEmptyWeekData(firstLoginDate: String): ChartData {
        val dateRange = chartsViewModel.getDateRangeFromFirstLogin(firstLoginDate)
        val categories = dateRange.map { chartsViewModel.formatDateForChart(it) }
        val emptyData = List(categories.size) { 0 }

        return ChartData(categories, emptyData, emptyData)
    }

    // Data class untuk menyimpan informasi grafik (kategori dan data aktivitas)
    data class ChartData(
        val categories: List<String>,
        val indoorData: List<Any>,
        val outdoorData: List<Any>
    )
}