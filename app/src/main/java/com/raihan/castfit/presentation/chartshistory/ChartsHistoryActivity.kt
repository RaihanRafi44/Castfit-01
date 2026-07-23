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
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAStyle
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AATitle
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
        binding.cvChartContainer.visibility = View.GONE
        binding.llEmptyState.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showChart() {
        binding.cvChartContainer.visibility = View.VISIBLE
        binding.llEmptyState.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.cvChartContainer.visibility = View.GONE
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
            .title("") 
            .subtitle("")
            .backgroundColor("#FAFAFA") // Menyesuaikan dengan latar belakang container
            .categories(chartData.categories.toTypedArray())
            .dataLabelsEnabled(true)
            .legendEnabled(false) // Menghilangkan tanda titik keterangan (legenda) di dalam grafik
            .yAxisTitle("Durasi (menit)")
            .yAxisGridLineWidth(0f) 
            .xAxisGridLineWidth(0f) 
            .xAxisVisible(true)
            .xAxisLabelsEnabled(true)
            .yAxisLabelsEnabled(true)
            .yAxisAllowDecimals(false) 
            .yAxisMin(0f)
            .stacking(AAChartStackingType.Normal)
            .tooltipEnabled(true)
            .borderRadius(0f) 
            .series(arrayOf(
                AASeriesElement()
                    .name("Indoor")
                    .data(chartData.indoorData.toTypedArray())
                    .color("#BB86FC"), // Purple Pastel

                AASeriesElement()
                    .name("Outdoor")
                    .data(chartData.outdoorData.toTypedArray())
                    .color("#03DAC5") // Teal Pastel
            ))

        val aaOptions = aaChartModel.aa_toAAOptions()
        
        // Menambahkan keterangan sumbu X melalui AAOptions
        aaOptions.xAxis?.title = AATitle()
            .text("Tanggal Aktivitas")
            .style(AAStyle().color("#757575").fontSize(12f))

        aaOptions.plotOptions?.column?.dataLabels = AADataLabels()
            .enabled(true)
            .style(AAStyle()
                .color("#424242") // Warna lebih gelap agar kontras
                .fontSize(14f)    // Ukuran diperbesar dari 11f ke 14f
                .fontWeight(AAChartFontWeightType.Bold))
            .formatter("""
            function () {
                return this.y > 0 ? this.y : ''; 
            }
        """.trimIndent())

        // Membuat batang lebih tebal dan jarak antar tanggal sangat rapat
        aaOptions.plotOptions?.column?.pointPadding = 0.02f // Batang sangat tebal
        aaOptions.plotOptions?.column?.groupPadding = 0.1f  // Jarak antar grup sangat rapat

        aaOptions.chart?.scrollablePlotArea(
            AAScrollablePlotArea()
                .minWidth(400) // Ukuran pas agar padat dan tebal
                .scrollPositionX(1f)
        )

        aaOptions.yAxis?.lineWidth = 1f
        aaOptions.yAxis?.lineColor = "#E0E0E0"
        aaOptions.xAxis?.lineColor = "#E0E0E0"

        // Menambah margin kiri sedikit agar teks "Durasi (menit)" tidak terpotong (tenggelam)
        // Nilai 50f - 60f biasanya cukup untuk menampung teks vertikal dan angka
        aaOptions.chart?.marginLeft = 55f

        aaChartView.aa_drawChartWithChartOptions(aaOptions)

        aaChartView.aa_drawChartWithChartOptions(aaOptions)
    }


    // Menyiapkan grafik kosong jika data riwayat tidak tersedia
    private fun setupEmptyChart(aaChartView: AAChartView) {
        val firstLoginDate = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getString("first_login_date", fullDateFormat.format(Date())) ?: fullDateFormat.format(Date())

        val emptyData = getEmptyWeekData(firstLoginDate)


        val aaChartModel = AAChartModel()
            .chartType(AAChartType.Column)
            .title("")
            .subtitle("")
            .categories(emptyData.categories.toTypedArray())
            .dataLabelsEnabled(false)
            .yAxisTitle("")
            .yAxisGridLineWidth(0f)
            .xAxisGridLineWidth(0f)
            .xAxisVisible(true)
            .yAxisLabelsEnabled(false)
            .stacking(AAChartStackingType.Normal)
            .borderRadius(12f)
            .series(arrayOf(
                AASeriesElement()
                    .name("Indoor")
                    .data(emptyData.indoorData.toTypedArray())
                    .color("#F0F0F0"), 

                AASeriesElement()
                    .name("Outdoor")
                    .data(emptyData.outdoorData.toTypedArray())
                    .color("#F9F9F9")
            ))

        val aaOptions = aaChartModel.aa_toAAOptions()
        aaOptions.chart?.scrollablePlotArea(
            AAScrollablePlotArea()
                .minWidth(0)
        )
        aaOptions.yAxis?.lineWidth = 0f
        aaOptions.xAxis?.lineColor = "#F0F0F0"

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