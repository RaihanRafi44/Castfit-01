package com.raihan.castfit.presentation.recommendation

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.raihan.castfit.data.datasource.physicalactivity.PhysicalDataSource
import com.raihan.castfit.data.model.PhysicalActivity
import com.raihan.castfit.data.model.User
import com.raihan.castfit.data.repository.ProgressActivityRepository
import com.raihan.castfit.data.repository.UserRepository
import com.raihan.castfit.utils.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class RecommendationViewModel (

    private val dataSource: PhysicalDataSource,
    private val userRepository: UserRepository,
    private val progressRepository: ProgressActivityRepository
) : ViewModel() {

    private val _indoorActivities = MutableLiveData<List<PhysicalActivity>>()
    val indoorActivities: LiveData<List<PhysicalActivity>> = _indoorActivities

    private val _outdoorActivities = MutableLiveData<List<PhysicalActivity>>()
    val outdoorActivities: LiveData<List<PhysicalActivity>> = _outdoorActivities

    // Tambahkan LiveData untuk tracking progress creation
    private val _progressCreationResult = MutableLiveData<Boolean>()
    val progressCreationResult: LiveData<Boolean> = _progressCreationResult

    fun loadActivitiesBasedOnWeather(condition: String) {
        viewModelScope.launch {
            val result = userRepository.getUserDateOfBirthAndAge()
                .firstOrNull { it is ResultWrapper.Success }

            val age = (result as? ResultWrapper.Success)?.payload?.second
            val allActivities = dataSource.getPhysicalActivitiesData()
            Log.d("RecommendationVM", "Filtered age: $age")

            val filteredActivities = if (age != null) {
                allActivities.filter { it.minAge <= age && it.maxAge >= age }
            } else {
                allActivities
            }

            // Atur kondisi cuaca untuk aktivitas
            val indoorOnlyWeather = listOf("rain", "drizzle", "snow", "storm", "thunderstorm",
                "heavy rain", "light rain", "moderate rain",
                "heavy snow", "light snow", "moderate snow",
                "blizzard", "sleet", "hail", "freezing rain",
                "thundery outbreaks possible", "patchy rain possible",
                "patchy snow possible", "patchy sleet possible",
                "patchy freezing drizzle possible", "mist", "fog",
                "overcast")
            val isBadWeather = indoorOnlyWeather.any { condition.contains(it, ignoreCase = true) }

            val indoor = filteredActivities.filter { it.type.equals("Indoor", ignoreCase = true) }
            val outdoor = if (isBadWeather) emptyList()
            else filteredActivities.filter { it.type.equals("Outdoor", ignoreCase = true) }

            _indoorActivities.postValue(indoor)
            _outdoorActivities.postValue(outdoor)
        }
    }

    fun addToProgress(activity: PhysicalActivity) {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            if (currentUser == null) {
                Log.e("ProgressDebug", "Cannot create progress: user not logged in")
                _progressCreationResult.postValue(false)
                return@launch
            }

            // Validasi user ID
            if (currentUser.id.isEmpty()) {
                Log.e("ProgressDebug", "Cannot create progress: user ID is empty")
                _progressCreationResult.postValue(false)
                return@launch
            }

            val dateStarted = getCurrentDate()
            val startedAt = getCurrentTime()

            Log.d("ProgressDebug", "Creating progress for ${activity.name}, user=${currentUser.id}")

            progressRepository.createProgress(activity, currentUser, dateStarted, startedAt)
                .collect { result ->
                    when (result) {
                        is ResultWrapper.Success -> {
                            Log.d("ProgressDebug", "Progress created successfully: ${result.payload}")
                            _progressCreationResult.postValue(true)
                        }
                        is ResultWrapper.Error -> {
                            Log.e("ProgressDebug", "Error creating progress: ${result.exception}")
                            _progressCreationResult.postValue(false)
                        }
                        else -> {}
                    }
                }
        }
    }

    fun getCurrentUser(): User? {
        return userRepository.getCurrentUser()
    }

    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun getCurrentTime(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
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


}

