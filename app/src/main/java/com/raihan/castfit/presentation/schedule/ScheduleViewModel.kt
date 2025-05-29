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

/*class ScheduleViewModel(

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

    private var currentUser: com.raihan.castfit.data.model.User? = null

    init {
        loadActivitiesBasedOnUserAge()
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.getUserData().collect { result ->
                when (result) {
                    is ResultWrapper.Success -> {
                        currentUser = result.payload
                        Log.d("ScheduleViewModel", "Current user loaded: ${currentUser?.fullName}")
                    }
                    is ResultWrapper.Error -> {
                        Log.e("ScheduleViewModel", "Failed to load current user: ${result.exception}")
                    }
                    else -> {}
                }
            }
        }
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

    fun saveSchedule(selectedActivityName: String, selectedDate: String, weatherStatus: String = "Cerah") {
        if (selectedActivityName.isEmpty() || selectedDate.isEmpty()) {
            Log.e("ScheduleViewModel", "Cannot save schedule: Missing required fields")
            _saveScheduleResult.postValue(false)
            return
        }

        if (currentUser == null) {
            Log.e("ScheduleViewModel", "Cannot save schedule: Current user not loaded")
            _saveScheduleResult.postValue(false)
            return
        }

        // Find the selected activity
        val selectedActivity = _activities.value?.find { it.name == selectedActivityName }
        if (selectedActivity == null) {
            Log.e("ScheduleViewModel", "Cannot save schedule: Activity not found")
            _saveScheduleResult.postValue(false)
            return
        }

        viewModelScope.launch {
            _isLoading.postValue(true)

            try {
                Log.d("ScheduleViewModel", "Saving schedule for activity: $selectedActivityName, date: $selectedDate")

                scheduleRepository.createSchedule(
                    activity = selectedActivity,
                    user = currentUser!!,
                    dateScheduled = selectedDate,
                    weatherStatus = weatherStatus
                ).collect { result ->
                    _isLoading.postValue(false)
                    when (result) {
                        is ResultWrapper.Success -> {
                            Log.d("ScheduleViewModel", "Schedule saved successfully")
                            _saveScheduleResult.postValue(true)
                        }
                        is ResultWrapper.Error -> {
                            Log.e("ScheduleViewModel", "Failed to save schedule: ${result.exception}")
                            _saveScheduleResult.postValue(false)
                        }
                        is ResultWrapper.Loading -> {
                            _isLoading.postValue(true)
                        }
                        else -> {
                            Log.w("ScheduleViewModel", "Unexpected result: $result")
                            _saveScheduleResult.postValue(false)
                        }
                    }
                }
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Log.e("ScheduleViewModel", "Error saving schedule", e)
                _saveScheduleResult.postValue(false)
            }
        }
    }

    fun resetSaveResult() {
        _saveScheduleResult.value = null
    }
}*/

/*class ScheduleViewModel(
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

    init {
        loadActivitiesBasedOnUserAge()
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.getUserData().collect { result ->
                when (result) {
                    is ResultWrapper.Success -> {
                        currentUser = result.payload
                        Log.d("ScheduleViewModel", "Current user loaded: ${currentUser?.fullName}")
                    }
                    is ResultWrapper.Error -> {
                        Log.e("ScheduleViewModel", "Failed to load current user: ${result.exception}")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadActivitiesBasedOnUserAge() {
        viewModelScope.launch {
            try {
                // Load all activities first
                allActivities = physicalDataSource.getPhysicalActivitiesData()
                Log.d("ScheduleViewModel", "Loaded ${allActivities.size} total activities")

                // Then filter based on user age
                userRepository.getUserDateOfBirthAndAge().collect { result ->
                    when (result) {
                        is ResultWrapper.Success -> {
                            val age = result.payload?.second
                            Log.d("ScheduleViewModel", "User age: $age")

                            val filtered = allActivities.filter { isActivitySuitableForAge(it, age) }
                            Log.d("ScheduleViewModel", "Filtered activities: ${filtered.size} suitable for age $age")
                            filtered.forEach { activity ->
                                Log.d("ScheduleViewModel", "Activity: ${activity.name} (${activity.minAge}-${activity.maxAge})")
                            }
                            _activities.postValue(filtered)
                        }
                        is ResultWrapper.Error -> {
                            Log.e("ScheduleViewModel", "Error loading user age, using all activities")
                            _activities.postValue(allActivities) // Fallback to all activities
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error loading activities", e)
                _activities.postValue(emptyList())
            }
        }
    }

    private fun isActivitySuitableForAge(activity: PhysicalActivity, age: Int?): Boolean {
        if (age == null) {
            Log.d("ScheduleViewModel", "Age is null, allowing activity: ${activity.name}")
            return true // Allow all activities if age is unknown
        }
        val suitable = age in (activity.minAge ?: 0)..(activity.maxAge ?: 100)
        Log.d("ScheduleViewModel", "Activity ${activity.name} suitable for age $age: $suitable")
        return suitable
    }

    fun saveSchedule(selectedActivityName: String, selectedDate: String, weatherStatus: String = "Cerah") {
        Log.d("ScheduleViewModel", "saveSchedule called with: activity='$selectedActivityName', date='$selectedDate'")

        if (selectedActivityName.isEmpty() || selectedDate.isEmpty()) {
            Log.e("ScheduleViewModel", "Cannot save schedule: Missing required fields")
            _saveScheduleResult.postValue(false)
            return
        }

        if (currentUser == null) {
            Log.e("ScheduleViewModel", "Cannot save schedule: Current user not loaded")
            _saveScheduleResult.postValue(false)
            return
        }

        // Find the selected activity from all activities (not just filtered ones)
        val selectedActivity = allActivities.find { it.name == selectedActivityName }
            ?: _activities.value?.find { it.name == selectedActivityName }

        if (selectedActivity == null) {
            Log.e("ScheduleViewModel", "Cannot save schedule: Activity '$selectedActivityName' not found")
            Log.d("ScheduleViewModel", "Available activities: ${_activities.value?.map { it.name }}")
            _saveScheduleResult.postValue(false)
            return
        }

        Log.d("ScheduleViewModel", "Found activity: ${selectedActivity.name} (ID: ${selectedActivity.id})")

        viewModelScope.launch {
            _isLoading.postValue(true)

            try {
                Log.d("ScheduleViewModel", "Saving schedule for activity: $selectedActivityName, date: $selectedDate")

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
                            Log.e("ScheduleViewModel", "Failed to save schedule: ${result.exception}")
                            result.exception?.printStackTrace()
                            _saveScheduleResult.postValue(false)
                        }
                        is ResultWrapper.Loading -> {
                            Log.d("ScheduleViewModel", "Saving schedule...")
                            _isLoading.postValue(true)
                        }
                        else -> {
                            Log.w("ScheduleViewModel", "Unexpected result: $result")
                            _saveScheduleResult.postValue(false)
                        }
                    }
                }
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Log.e("ScheduleViewModel", "Error saving schedule", e)
                e.printStackTrace()
                _saveScheduleResult.postValue(false)
            }
        }
    }

    fun resetSaveResult() {
        _saveScheduleResult.value = null
    }
}*/

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

    // Panggil ini dari Fragment atau Activity
    fun loadInitialData() {
        loadCurrentUser()
        loadActivitiesBasedOnUserAge()
    }

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
                            // Fallback: gunakan data dari Firebase Auth
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
                // Fallback: gunakan data dari Firebase Auth
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    currentUser = firebaseUser.toUser()
                    Log.d("ScheduleViewModel", "Fallback to Firebase Auth user: ${currentUser?.fullName}")
                }
            }
        }
    }

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

    private fun isActivitySuitableForAge(activity: PhysicalActivity, age: Int?): Boolean {
        if (age == null) return true
        return age in (activity.minAge ?: 0)..(activity.maxAge ?: 100)
    }

    fun saveSchedule(selectedActivityName: String, selectedDate: String, weatherStatus: String = "Cerah") {
        Log.d("ScheduleViewModel", "saveSchedule called with: activity='$selectedActivityName', date='$selectedDate'")

        if (selectedActivityName.isEmpty() || selectedDate.isEmpty()) {
            Log.e("ScheduleViewModel", "Input validation failed - activity or date is empty")
            _saveScheduleResult.postValue(false)
            return
        }

        // Pastikan currentUser tidak null
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

    fun resetSaveResult() {
        _saveScheduleResult.value = null
    }
}

