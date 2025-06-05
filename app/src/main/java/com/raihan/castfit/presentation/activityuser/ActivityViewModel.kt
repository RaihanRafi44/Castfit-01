package com.raihan.castfit.presentation.activityuser

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.raihan.castfit.data.model.HistoryActivity
import com.raihan.castfit.data.model.ProgressActivity
import com.raihan.castfit.data.model.ScheduleActivity
import com.raihan.castfit.data.repository.HistoryActivityRepository
import com.raihan.castfit.data.repository.ProgressActivityRepository
import com.raihan.castfit.data.repository.ScheduleActivityRepository
import com.raihan.castfit.utils.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityViewModel(
    private val progressRepository: ProgressActivityRepository,
    private val historyRepository: HistoryActivityRepository,
    private val scheduleRepository: ScheduleActivityRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    // Track delete operation result
    private val _deleteOperationResult = MutableLiveData<Boolean?>()
    val deleteOperationResult: LiveData<Boolean?> = _deleteOperationResult

    // Track finish operation result
    private val _finishOperationResult = MutableLiveData<Boolean?>()
    val finishOperationResult: LiveData<Boolean?> = _finishOperationResult

    // Track delete history operation result
    private val _deleteHistoryResult = MutableLiveData<Boolean?>()
    val deleteHistoryResult: LiveData<Boolean?> = _deleteHistoryResult

    // Track delete schedule operation result
    private val _deleteScheduleResult = MutableLiveData<Boolean?>()
    val deleteScheduleResult: LiveData<Boolean?> = _deleteScheduleResult

    // Track weather check result for scheduled activities
    private val _weatherCheckResult = MutableLiveData<WeatherCheckResult?>()
    val weatherCheckResult: LiveData<WeatherCheckResult?> = _weatherCheckResult

    // Track start scheduled activity result
    private val _startScheduledActivityResult = MutableLiveData<Boolean?>()
    val startScheduledActivityResult: LiveData<Boolean?> = _startScheduledActivityResult

    fun getAllProgress() = progressRepository.getUserProgressData().asLiveData(Dispatchers.IO)

    fun getAllHistory() = historyRepository.getUserHistoryData().asLiveData(Dispatchers.IO)

    fun getAllSchedule() = scheduleRepository.getUserScheduleActivity().asLiveData(Dispatchers.IO)

    fun removeProgress(item: ProgressActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if user is logged in
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("ActivityViewModel", "Cannot delete: User not logged in")
                _deleteOperationResult.postValue(false)
                return@launch
            }

            // Check if the progress belongs to current user
            if (item.userId != currentUserId) {
                Log.e("ActivityViewModel", "Cannot delete: Progress doesn't belong to current user")
                _deleteOperationResult.postValue(false)
                return@launch
            }

            // Validasi ID sebelum delete
            if (item.id == null || item.id <= 0) {
                Log.e("ActivityViewModel", "Cannot delete: Invalid ID (${item.id})")
                _deleteOperationResult.postValue(false)
                return@launch
            }

            Log.d("ActivityViewModel", "Attempting to delete activity: ${item.physicalActivityName} with ID: ${item.id}")

            progressRepository.deleteProgress(item).collect { result ->
                when (result) {
                    is ResultWrapper.Success -> {
                        Log.d("ActivityViewModel", "Successfully deleted activity: ${item.physicalActivityName} with ID: ${item.id}")
                        _deleteOperationResult.postValue(true)
                    }
                    is ResultWrapper.Error -> {
                        Log.e("ActivityViewModel", "Failed to delete activity: ${item.physicalActivityName}", result.exception)
                        _deleteOperationResult.postValue(false)
                    }
                    is ResultWrapper.Loading -> {
                        Log.d("ActivityViewModel", "Deleting activity in progress...")
                    }
                    else -> {
                        Log.w("ActivityViewModel", "Unexpected result state: $result")
                        _deleteOperationResult.postValue(false)
                    }
                }
            }
        }
    }

    fun removeSchedule(item: ScheduleActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if user is logged in
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("ActivityViewModel", "Cannot delete schedule: User not logged in")
                _deleteScheduleResult.postValue(false)
                return@launch
            }

            // Check if the schedule belongs to current user
            if (item.userId != currentUserId) {
                Log.e("ActivityViewModel", "Cannot delete schedule: Schedule doesn't belong to current user")
                _deleteScheduleResult.postValue(false)
                return@launch
            }

            // Validasi ID sebelum delete
            if (item.id == null || item.id <= 0) {
                Log.e("ActivityViewModel", "Cannot delete schedule: Invalid ID (${item.id})")
                _deleteScheduleResult.postValue(false)
                return@launch
            }

            Log.d("ActivityViewModel", "Attempting to delete schedule: ${item.physicalActivityName} with ID: ${item.id}")

            scheduleRepository.deleteSchedule(item).collect { result ->
                when (result) {
                    is ResultWrapper.Success -> {
                        Log.d("ActivityViewModel", "Successfully deleted schedule: ${item.physicalActivityName} with ID: ${item.id}")
                        _deleteScheduleResult.postValue(true)
                    }
                    is ResultWrapper.Error -> {
                        Log.e("ActivityViewModel", "Failed to delete schedule: ${item.physicalActivityName}", result.exception)
                        _deleteScheduleResult.postValue(false)
                    }
                    is ResultWrapper.Loading -> {
                        Log.d("ActivityViewModel", "Deleting schedule in progress...")
                    }
                    else -> {
                        Log.w("ActivityViewModel", "Unexpected result state: $result")
                        _deleteScheduleResult.postValue(false)
                    }
                }
            }
        }
    }

    fun checkWeatherForScheduledActivity(schedule: ScheduleActivity, currentWeatherCondition: String?) {
        viewModelScope.launch {
            try {
                Log.d("ActivityViewModel", "Checking weather for activity: ${schedule.physicalActivityName}")
                Log.d("ActivityViewModel", "Activity type: ${schedule.physicalActivityType}")
                Log.d("ActivityViewModel", "Current weather: $currentWeatherCondition")

                // Check if activity type is outdoor
                val isOutdoorActivity = schedule.physicalActivityType?.equals("Outdoor", ignoreCase = true) == true

                if (!isOutdoorActivity) {
                    // Indoor activity - always allowed
                    Log.d("ActivityViewModel", "Indoor activity - weather check passed")
                    _weatherCheckResult.postValue(
                        WeatherCheckResult(
                            isWeatherSuitable = true,
                            schedule = schedule,
                            message = "Konfirmasi untuk memulai aktivitas indoor '${schedule.physicalActivityName}'",
                            confirmationTitle = "Mulai Aktivitas?"
                        )
                    )
                    return@launch
                }

                // Check if weather condition is suitable for outdoor activities
                val isWeatherSuitable = isWeatherSuitableForOutdoor(currentWeatherCondition)

                if (isWeatherSuitable) {
                    Log.d("ActivityViewModel", "Outdoor activity - weather is suitable")
                    _weatherCheckResult.postValue(
                        WeatherCheckResult(
                            isWeatherSuitable = true,
                            schedule = schedule,
                            message = "Cuaca mendukung untuk aktivitas outdoor '${schedule.physicalActivityName}'. Apakah Anda ingin memulainya sekarang?",
                            confirmationTitle = "Mulai Aktivitas?"
                        )
                    )
                } else {
                    Log.d("ActivityViewModel", "Outdoor activity - weather is not suitable")
                    _weatherCheckResult.postValue(
                        WeatherCheckResult(
                            isWeatherSuitable = false,
                            schedule = schedule,
                            message = "Cuaca saat ini tidak mendukung untuk aktivitas yang anda pilih. Anda bisa melakukan aktivitas ini nanti saat cuaca mendukung atau membatalkan aktivitas ini.",
                            confirmationTitle = "Cuaca Tidak Mendukung"
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("ActivityViewModel", "Error checking weather for scheduled activity", e)
                _weatherCheckResult.postValue(
                    WeatherCheckResult(
                        isWeatherSuitable = false,
                        schedule = schedule,
                        message = "Terjadi kesalahan saat memeriksa kondisi cuaca. Silakan coba lagi.",
                        confirmationTitle = "Error"
                    )
                )
            }
        }
    }

    private fun isWeatherSuitableForOutdoor(weatherCondition: String?): Boolean {
        if (weatherCondition.isNullOrEmpty()) {
            Log.w("ActivityViewModel", "Weather condition is null or empty")
            return false
        }

        val unsuitableWeatherConditions = listOf(
            "rain", "drizzle", "snow", "storm", "thunderstorm",
            "heavy rain", "light rain", "moderate rain",
            "heavy snow", "light snow", "moderate snow",
            "blizzard", "sleet", "hail", "freezing rain",
            "thundery outbreaks possible", "patchy rain possible",
            "patchy snow possible", "patchy sleet possible",
            "patchy freezing drizzle possible", "mist", "fog",
            "overcast"
        )

        val weatherLower = weatherCondition.lowercase(Locale.getDefault())
        Log.d("ActivityViewModel", "Checking weather condition: $weatherLower")

        val isUnsuitableWeather = unsuitableWeatherConditions.any { condition ->
            weatherLower.contains(condition)
        }

        Log.d("ActivityViewModel", "Weather is ${if (isUnsuitableWeather) "not " else ""}suitable for outdoor activity")
        return !isUnsuitableWeather
    }

    fun startScheduledActivity(schedule: ScheduleActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUserId = firebaseAuth.currentUser?.uid
                if (currentUserId == null) {
                    Log.e("ActivityViewModel", "Cannot start activity: User not logged in")
                    _startScheduledActivityResult.postValue(false)
                    return@launch
                }

                if (schedule.userId != currentUserId) {
                    Log.e("ActivityViewModel", "Cannot start activity: Schedule doesn't belong to current user")
                    _startScheduledActivityResult.postValue(false)
                    return@launch
                }

                Log.d("ActivityViewModel", "Starting scheduled activity: ${schedule.physicalActivityName}")

                // Create current date and time
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

                // Create progress activity from schedule
                val progressActivity = ProgressActivity(
                    userId = currentUserId,
                    physicalActivityId = schedule.physicalActivityId,
                    physicalActivityName = schedule.physicalActivityName,
                    dateStarted = currentDate,
                    startedAt = currentTime
                )

                // Add to progress
                progressRepository.createProgressByEntity(progressActivity).collect { createResult ->
                    when (createResult) {
                        is ResultWrapper.Success -> {
                            Log.d("ActivityViewModel", "Successfully created progress activity")
                            // After successfully creating progress, delete from schedule
                            scheduleRepository.deleteSchedule(schedule).collect { deleteResult ->
                                when (deleteResult) {
                                    is ResultWrapper.Success -> {
                                        Log.d("ActivityViewModel", "Successfully moved scheduled activity to progress")
                                        _startScheduledActivityResult.postValue(true)
                                    }
                                    is ResultWrapper.Error -> {
                                        Log.e("ActivityViewModel", "Failed to delete schedule after creating progress", deleteResult.exception)
                                        _startScheduledActivityResult.postValue(false)
                                    }
                                    is ResultWrapper.Loading -> {
                                        Log.d("ActivityViewModel", "Deleting schedule...")
                                    }
                                    else -> {
                                        Log.w("ActivityViewModel", "Unexpected delete result: $deleteResult")
                                        _startScheduledActivityResult.postValue(false)
                                    }
                                }
                            }
                        }
                        is ResultWrapper.Error -> {
                            Log.e("ActivityViewModel", "Failed to create progress activity", createResult.exception)
                            _startScheduledActivityResult.postValue(false)
                        }
                        is ResultWrapper.Loading -> {
                            Log.d("ActivityViewModel", "Creating progress activity...")
                        }
                        else -> {
                            Log.w("ActivityViewModel", "Unexpected create result: $createResult")
                            _startScheduledActivityResult.postValue(false)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ActivityViewModel", "Error starting scheduled activity", e)
                _startScheduledActivityResult.postValue(false)
            }
        }
    }

    fun finishActivity(item: ProgressActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if user is logged in
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("ActivityViewModel", "Cannot finish: User not logged in")
                _finishOperationResult.postValue(false)
                return@launch
            }

            // Check if the progress belongs to current user
            if (item.userId != currentUserId) {
                Log.e("ActivityViewModel", "Cannot finish: Progress doesn't belong to current user")
                _finishOperationResult.postValue(false)
                return@launch
            }

            // Validasi ID sebelum finish
            if (item.id == null || item.id <= 0) {
                Log.e("ActivityViewModel", "Cannot finish: Invalid ID (${item.id})")
                _finishOperationResult.postValue(false)
                return@launch
            }

            Log.d("ActivityViewModel", "Attempting to finish activity: ${item.physicalActivityName} with ID: ${item.id}")

            try {
                // Buat data untuk history
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

                // Hitung durasi (contoh: dari dateStarted sampai sekarang)
                /*val duration = item.dateStarted?.let { item.startedAt?.let { it1 ->
                    calculateDuration(it, it1)
                } }*/
                val date = item.dateStarted
                val time = item.startedAt
                val duration = if (date != null && time != null) {
                    val result = calculateDuration(date, time)
                    Log.d("ActivityViewModel", "Result from calculateDuration(): $result")
                    result
                } else {
                    null
                }


                Log.d("ActivityViewModel", "Creating history with duration: $duration minutes")

                // FIXED: Implementasi sequential untuk menghindari race condition
                if (duration != null) {
                    // Step 1: Buat history terlebih dahulu
                    historyRepository.createHistory(
                        progress = item,
                        dateEnded = currentDate,
                        completedAt = currentTime,
                        duration = duration
                    ).collect { historyResult ->
                        when (historyResult) {
                            is ResultWrapper.Success -> {
                                Log.d("ActivityViewModel", "Successfully created history for: ${item.physicalActivityName}")

                                // Step 2: Setelah history berhasil dibuat, baru hapus dari progress
                                viewModelScope.launch(Dispatchers.IO) {
                                    progressRepository.deleteProgress(item).collect { deleteResult ->
                                        when (deleteResult) {
                                            is ResultWrapper.Success -> {
                                                Log.d("ActivityViewModel", "Successfully moved activity to history: ${item.physicalActivityName}")
                                                _finishOperationResult.postValue(true)
                                            }
                                            is ResultWrapper.Error -> {
                                                Log.e("ActivityViewModel", "Failed to delete from progress after creating history", deleteResult.exception)
                                                _finishOperationResult.postValue(false)
                                            }
                                            is ResultWrapper.Loading -> {
                                                Log.d("ActivityViewModel", "Deleting from progress...")
                                            }
                                            else -> {
                                                Log.w("ActivityViewModel", "Unexpected delete result: $deleteResult")
                                                _finishOperationResult.postValue(false)
                                            }
                                        }
                                    }
                                }
                            }
                            is ResultWrapper.Error -> {
                                Log.e("ActivityViewModel", "Failed to create history", historyResult.exception)
                                _finishOperationResult.postValue(false)
                            }
                            is ResultWrapper.Loading -> {
                                Log.d("ActivityViewModel", "Creating history...")
                            }
                            else -> {
                                Log.w("ActivityViewModel", "Unexpected history result: $historyResult")
                                _finishOperationResult.postValue(false)
                            }
                        }
                    }
                } else {
                    Log.e("ActivityViewModel", "Cannot calculate duration")
                    _finishOperationResult.postValue(false)
                }
            } catch (e: Exception) {
                Log.e("ActivityViewModel", "Error finishing activity", e)
                _finishOperationResult.postValue(false)
            }
        }
    }

    fun removeHistory(item: HistoryActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if user is logged in
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("ActivityViewModel", "Cannot delete history: User not logged in")
                _deleteHistoryResult.postValue(false)
                return@launch
            }

            // FIXED: Check if the history belongs to current user
            if (item.userId != currentUserId) {
                Log.e("ActivityViewModel", "Cannot delete history: History doesn't belong to current user")
                _deleteHistoryResult.postValue(false)
                return@launch
            }

            // Validasi ID sebelum delete
            if (item.id == null || item.id <= 0) {
                Log.e("ActivityViewModel", "Cannot delete history: Invalid ID (${item.id})")
                _deleteHistoryResult.postValue(false)
                return@launch
            }

            Log.d("ActivityViewModel", "Attempting to delete history with ID: ${item.id}")

            historyRepository.deleteHistory(item).collect { result ->
                when (result) {
                    is ResultWrapper.Success -> {
                        Log.d("ActivityViewModel", "Successfully deleted history with ID: ${item.id}")
                        _deleteHistoryResult.postValue(true)
                    }
                    is ResultWrapper.Error -> {
                        Log.e("ActivityViewModel", "Failed to delete history", result.exception)
                        _deleteHistoryResult.postValue(false)
                    }
                    is ResultWrapper.Loading -> {
                        Log.d("ActivityViewModel", "Deleting history in progress...")
                    }
                    else -> {
                        Log.w("ActivityViewModel", "Unexpected result state: $result")
                        _deleteHistoryResult.postValue(false)
                    }
                }
            }
        }
    }

    fun calculateDuration(dateStarted: String, startedAt: String): Int {
        return try {
            Log.d("ActivityViewModel", "Input - dateStarted: '$dateStarted', startedAt: '$startedAt'")

            // Coba beberapa format yang mungkin
            val possibleFormats = listOf(
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "HH:mm:ss",
                "HH:mm"
            )

            var startDateTime: Date? = null

            // Jika startedAt sudah berisi tanggal lengkap
            if (startedAt.contains("-")) {
                for (format in possibleFormats) {
                    try {
                        startDateTime = SimpleDateFormat(format, Locale.getDefault()).parse(startedAt)
                        break
                    } catch (e: Exception) {
                        Log.d("ActivityViewModel", "Failed to parse with format: $format")
                    }
                }
            } else {
                // Jika startedAt hanya berisi waktu, gabungkan dengan dateStarted
                val combinedDateTime = "$dateStarted $startedAt"
                for (format in possibleFormats) {
                    try {
                        startDateTime = SimpleDateFormat(format, Locale.getDefault()).parse(combinedDateTime)
                        break
                    } catch (e: Exception) {
                        Log.d("ActivityViewModel", "Failed to parse combined datetime with format: $format")
                    }
                }
            }

            if (startDateTime == null) {
                Log.e("ActivityViewModel", "Could not parse start time with any format")
                return 0
            }

            val currentTime = Date()
            val diffInMillis = currentTime.time - startDateTime.time

            Log.d("ActivityViewModel", "Start time: $startDateTime")
            Log.d("ActivityViewModel", "Current time: $currentTime")
            Log.d("ActivityViewModel", "Difference in millis: $diffInMillis")

            if (diffInMillis < 0) {
                Log.w("ActivityViewModel", "Start time is in the future, returning 0")
                return 0
            }

            val diffInMinutes = (diffInMillis / (1000 * 60)).toInt()

            Log.d("ActivityViewModel", "Calculated duration: $diffInMinutes minutes")
            Log.d("ActivityViewModel", "Difference in millis: $diffInMillis")
            Log.d("ActivityViewModel", "Difference in seconds: ${diffInMillis / 1000}")

            // Pastikan minimal 1 menit jika ada selisih waktu
            //return if (diffInMinutes == 0 && diffInMillis > 0) 1 else diffInMinutes
            return diffInMinutes

        } catch (e: Exception) {
            Log.e("ActivityViewModel", "Error calculating duration", e)
            Log.e("ActivityViewModel", "dateStarted: '$dateStarted', startedAt: '$startedAt'")
            0 // Default jika error
        }
    }

    suspend fun checkIfUserHasProgressSuspend(): Boolean {
        val result = progressRepository.getUserProgressData().first {
            it !is ResultWrapper.Loading // tunggu sampai bukan loading
        }

        return when (result) {
            is ResultWrapper.Success -> {
                val hasProgress = result.payload?.isNotEmpty() == true
                Log.d("RecommendationDebug", "Has Progress? $hasProgress")
                hasProgress
            }
            is ResultWrapper.Empty -> {
                Log.d("RecommendationDebug", "No progress found.")
                false
            }
            is ResultWrapper.Error -> {
                Log.e("RecommendationDebug", "Error while checking progress", result.exception)
                false
            }
            else -> false
        }
    }


    fun resetDeleteResult() {
        _deleteOperationResult.value = null
    }

    fun resetFinishResult() {
        _finishOperationResult.value = null
    }

    fun resetDeleteHistoryResult() {
        _deleteHistoryResult.value = null
    }

    fun resetDeleteScheduleResult() {
        _deleteScheduleResult.value = null
    }

    fun resetWeatherCheckResult() {
        _weatherCheckResult.value = null
    }

    fun resetStartScheduledActivityResult() {
        _startScheduledActivityResult.value = null
    }

    // Data class untuk weather check result
    data class WeatherCheckResult(
        val isWeatherSuitable: Boolean,
        val schedule: ScheduleActivity,
        val message: String,
        val confirmationTitle: String
    )
}
