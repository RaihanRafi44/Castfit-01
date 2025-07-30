package com.raihan.castfit.presentation.schedule

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.raihan.castfit.data.datasource.physicalactivity.PhysicalDataSourceImpl
import com.raihan.castfit.data.model.PhysicalActivity
import com.raihan.castfit.data.model.User
import com.raihan.castfit.data.model.toUser
import com.raihan.castfit.data.repository.ScheduleActivityRepository
import com.raihan.castfit.data.repository.UserRepository
import com.raihan.castfit.utils.ResultWrapper
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val userRepository: UserRepository,
    private val scheduleRepository: ScheduleActivityRepository
) : ViewModel() {

    private val physicalDataSource = PhysicalDataSourceImpl()

    private val _activities = MutableLiveData<List<PhysicalActivity>>()
    val activities: LiveData<List<PhysicalActivity>> get() = _activities

    private val _saveScheduleResult = MutableLiveData<Boolean?>()
    val saveScheduleResult: LiveData<Boolean?> = _saveScheduleResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentUser: User? = null
    private var allActivities: List<PhysicalActivity> = emptyList()

    // Memuat data awal: user saat ini dan aktivitas berdasarkan usia
    fun loadInitialData() {
        loadCurrentUser()
        loadActivitiesBasedOnUserAge()
    }

    // Mengambil data user dari repository atau fallback ke Firebase Auth jika gagal
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                userRepository.getUserData().collect { result ->
                    when (result) {
                        is ResultWrapper.Success -> {
                            currentUser = result.payload
                            Log.d("ScheduleViewModel", "Current user loaded: ${currentUser?.fullName}")
                        }
                        is ResultWrapper.Error -> {
                            Log.e("ScheduleViewModel", "Failed to load current user: ${result.exception}")

                            val firebaseUser = FirebaseAuth.getInstance().currentUser
                            if (firebaseUser != null) {
                                currentUser = firebaseUser.toUser()
                                Log.d("ScheduleViewModel", "Using Firebase Auth user: ${currentUser?.fullName}")
                            }
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error loading user data", e)

                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    currentUser = firebaseUser.toUser()
                    Log.d("ScheduleViewModel", "Fallback to Firebase Auth user: ${currentUser?.fullName}")
                }
            }
        }
    }

    // Mengambil seluruh aktivitas lalu menyaring berdasarkan usia user
    private fun loadActivitiesBasedOnUserAge() {
        viewModelScope.launch {
            try {
                allActivities = physicalDataSource.getPhysicalActivitiesData()
                userRepository.getUserDateOfBirthAndAge().collect { result ->
                    when (result) {
                        is ResultWrapper.Success -> {
                            val age = result.payload?.second
                            val filtered = allActivities.filter { isActivitySuitableForAge(it, age) }
                            _activities.postValue(filtered)
                        }
                        is ResultWrapper.Error -> {
                            _activities.postValue(allActivities)
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                _activities.postValue(emptyList())
            }
        }
    }

    // Mengecek apakah aktivitas cocok dengan usia user
    private fun isActivitySuitableForAge(activity: PhysicalActivity, age: Int?): Boolean {
        if (age == null) return true
        return age in (activity.minAge)..(activity.maxAge)
    }

    // Menyimpan jadwal aktivitas fisik
    fun saveSchedule(selectedActivityName: String, selectedDate: String, weatherStatus: String = "Cerah") {
        Log.d("ScheduleViewModel", "saveSchedule called with: activity='$selectedActivityName', date='$selectedDate'")

        if (selectedActivityName.isEmpty() || selectedDate.isEmpty()) {
            Log.e("ScheduleViewModel", "Input validation failed - activity or date is empty")
            _saveScheduleResult.postValue(false)
            return
        }


        if (currentUser == null) {
            Log.e("ScheduleViewModel", "Current user is null, trying to get from Firebase Auth")
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                currentUser = firebaseUser.toUser()
                Log.d("ScheduleViewModel", "Got user from Firebase Auth: ${currentUser?.fullName}")
            } else {
                Log.e("ScheduleViewModel", "No user logged in")
                _saveScheduleResult.postValue(false)
                return
            }
        }

        val selectedActivity = allActivities.find { it.name == selectedActivityName }
            ?: _activities.value?.find { it.name == selectedActivityName }

        if (selectedActivity == null) {
            Log.e("ScheduleViewModel", "Selected activity not found: $selectedActivityName")
            _saveScheduleResult.postValue(false)
            return
        }

        Log.d("ScheduleViewModel", "Found activity: ${selectedActivity.name}, ID: ${selectedActivity.id}")
        Log.d("ScheduleViewModel", "Current user: ${currentUser?.fullName}, ID: ${currentUser?.id}")

        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                scheduleRepository.createSchedule(
                    activity = selectedActivity,
                    user = currentUser!!,
                    dateScheduled = selectedDate,
                    weatherStatus = weatherStatus
                ).collect { result ->
                    _isLoading.postValue(false)
                    when (result) {
                        is ResultWrapper.Success -> {
                            Log.d("ScheduleViewModel", "Schedule saved successfully: ${result.payload}")
                            _saveScheduleResult.postValue(result.payload == true)
                        }
                        is ResultWrapper.Error -> {
                            Log.e("ScheduleViewModel", "Failed to save schedule", result.exception)
                            _saveScheduleResult.postValue(false)
                        }
                        is ResultWrapper.Loading -> {
                            Log.d("ScheduleViewModel", "Saving schedule...")
                        }
                        else -> {
                            Log.w("ScheduleViewModel", "Unexpected result: $result")
                            _saveScheduleResult.postValue(false)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Exception while saving schedule", e)
                _isLoading.postValue(false)
                _saveScheduleResult.postValue(false)
            }
        }
    }

    // Mereset status hasil penyimpanan agar bisa digunakan ulang
    fun resetSaveResult() {
        _saveScheduleResult.value = null
    }
}

