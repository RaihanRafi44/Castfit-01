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

    // Cache physical activities data untuk performance
    private val physicalActivitiesMap by lazy {
        physicalDataSource.getPhysicalActivitiesData().associateBy { it.name }
    }

    fun getAllHistory() = historyRepository.getUserHistoryData().asLiveData(Dispatchers.IO)

    /**
     * Get activity type based on activity name from PhysicalDataSource
     */
    fun getActivityType(activityName: String?): String {
        return physicalActivitiesMap[activityName]?.type ?: "Indoor" // Default to Indoor if not found
    }

    /**
     * Get the last 7 days date range
     * This method handles the sliding window logic where dates update every day
     */
    fun getLast7DaysRange(): List<Date> {
        val calendar = Calendar.getInstance()
        val dates = mutableListOf<Date>()

        // Start from 6 days ago to today (total 7 days)
        calendar.add(Calendar.DAY_OF_YEAR, -6)

        for (i in 0..6) {
            dates.add(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return dates
    }

    /**
     * Get date range from user's first login until now
     * This is used for the initial period when user has less than 7 days of activity
     */
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

                // If more than 7 days, return only last 7 days
                return if (dates.size > 7) {
                    dates.takeLast(7)
                } else {
                    dates
                }
            }
        } catch (e: Exception) {
            // If parsing fails, return last 7 days
            return getLast7DaysRange()
        }

        return getLast7DaysRange()
    }

    /**
     * Format date for chart categories
     */
    fun formatDateForChart(date: Date): String {
        return dateFormat.format(date)
    }

    /**
     * Format duration for display
     * Returns appropriate string based on duration rules
     */
    fun formatDuration(durationInSeconds: Int): Any {
        val durationInMinutes = durationInSeconds / 60
        return when {
            durationInMinutes == 0 -> 0
            durationInMinutes < 1 -> "<1 menit"
            else -> durationInMinutes
        }
    }

    /**
     * Check if current date is within the first 7 days since user registration
     */
    fun isWithinFirstWeek(firstLoginDate: String): Boolean {
        try {
            val firstLogin = fullDateFormat.parse(firstLoginDate)
            if (firstLogin != null) {
                val calendar = Calendar.getInstance()
                calendar.time = firstLogin
                calendar.add(Calendar.DAY_OF_YEAR, 7)

                val sevenDaysAfterFirstLogin = calendar.time
                val today = Date()

                return today <= sevenDaysAfterFirstLogin
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }
}