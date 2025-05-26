package com.raihan.castfit.presentation.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raihan.castfit.data.datasource.physicalactivity.PhysicalDataSourceImpl
import com.raihan.castfit.data.model.PhysicalActivity
import com.raihan.castfit.data.repository.UserRepository
import com.raihan.castfit.utils.ResultWrapper
import kotlinx.coroutines.launch

class ScheduleViewModel(

    private val userRepository: UserRepository

) : ViewModel() {

    private val physicalDataSource = PhysicalDataSourceImpl()

    private val _activities = MutableLiveData<List<PhysicalActivity>>()
    val activities: LiveData<List<PhysicalActivity>> get() = _activities

    init {
        loadActivitiesBasedOnUserAge()
    }

    private fun loadActivities() {
        val data = physicalDataSource.getPhysicalActivitiesData()
        _activities.value = data
    }

    private fun loadActivitiesBasedOnUserAge() {
        viewModelScope.launch {
            userRepository.getUserDateOfBirthAndAge().collect { result ->
                when (result) {
                    is ResultWrapper.Success -> {
                        val age = result.payload?.second
                        val data = physicalDataSource.getPhysicalActivitiesData()
                        val filtered = data.filter { isActivitySuitableForAge(it, age) }
                        _activities.postValue(filtered)
                    }
                    is ResultWrapper.Error -> {
                        _activities.postValue(emptyList()) // Handle error gracefully
                    }
                    else -> {}
                }
            }
        }
    }

    private fun isActivitySuitableForAge(activity: PhysicalActivity, age: Int?): Boolean {
        if (age == null) return false
        return age in (activity.minAge ?: 0)..(activity.maxAge ?: 100)
    }
}
