package com.raihan.castfit.presentation.chartshistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.raihan.castfit.data.repository.HistoryActivityRepository
import com.raihan.castfit.data.datasource.physicalactivity.PhysicalDataSource
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*

class ChartsHistoryViewModel(
    private val historyRepository: HistoryActivityRepository,
    private val physicalDataSource: PhysicalDataSource
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Peta aktivitas fisik berdasarkan nama
    private val physicalActivitiesMap by lazy {
        physicalDataSource.getPhysicalActivitiesData().associateBy { it.name }
    }

    fun getAllHistory() = historyRepository.getUserHistoryData().asLiveData(Dispatchers.IO)

    // Mengambil tipe aktivitas berdasarkan nama
    fun getActivityType(activityName: String?): String {
        return physicalActivitiesMap[activityName]?.type ?: "Indoor" // Default ke Indoor
    }

    // Menghasilkan daftar 7 tanggal terakhir (termasuk hari ini) untuk grafik
    fun getLast7DaysRange(): List<Date> {
        val calendar = Calendar.getInstance()
        val dates = mutableListOf<Date>()

        // Start dari 6 hari yang lalu hingga hari ini (total 7 hari)
        calendar.add(Calendar.DAY_OF_YEAR, -6)

        for (i in 0..6) {
            dates.add(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return dates
    }

    // Menghasilkan rentang tanggal dari tanggal login pertama hingga hari ini (maks. 7 hari)
    fun getDateRangeFromFirstLogin(firstLoginDate: String): List<Date> {
        val calendar = Calendar.getInstance()
        val today = calendar.time

        try {
            val firstLogin = fullDateFormat.parse(firstLoginDate)
            if (firstLogin != null) {
                calendar.time = firstLogin
                val dates = mutableListOf<Date>()

                while (calendar.time <= today) {
                    dates.add(calendar.time)
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                // Jika lebih dari 7 hari, akan menampilkan hanya 7 hari terakhir
                return if (dates.size > 7) {
                    dates.takeLast(7)
                } else {
                    dates
                }
            }
        } catch (e: Exception) {
            // Jika parsing gagal, kembalikan 7 hari terakhir
            return getLast7DaysRange()
        }

        return getLast7DaysRange()
    }

    // Format tanggal menjadi string pendek (dd/MM) untuk ditampilkan di grafik
    fun formatDateForChart(date: Date): String {
        return dateFormat.format(date)
    }
}