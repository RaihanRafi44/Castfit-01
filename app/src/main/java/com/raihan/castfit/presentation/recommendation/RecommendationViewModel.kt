package com.raihan.castfit.presentation.recommendation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.raihan.castfit.data.datasource.physicalactivity.PhysicalDataSource
import com.raihan.castfit.data.model.PhysicalActivity
import com.raihan.castfit.data.repository.ProgressActivityRepository
import com.raihan.castfit.data.repository.UserRepository
import com.raihan.castfit.utils.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class RecommendationViewModel (
    private val dataSource: PhysicalDataSource,
    private val userRepository: UserRepository,
    private val progressRepository: ProgressActivityRepository
) : ViewModel() {

    private val _indoorActivities = MutableLiveData<List<PhysicalActivity>>()
    val indoorActivities: LiveData<List<PhysicalActivity>> = _indoorActivities

    private val _outdoorActivities = MutableLiveData<List<PhysicalActivity>>()
    val outdoorActivities: LiveData<List<PhysicalActivity>> = _outdoorActivities

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
            val indoorOnlyWeather = listOf("Storm", "Rain", "Snow", "Thunderstorm")
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
            userRepository.getUserData().collect { result ->
                val user = (result as? ResultWrapper.Success)?.payload
                if (user != null) {
                    val dateStarted = getCurrentDate()
                    val startedAt = getCurrentTime()
                    progressRepository.createProgress(activity, user, dateStarted, startedAt).asLiveData(
                        Dispatchers.IO)
                }
            }
        }
    }


    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun getCurrentTime(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }




}

