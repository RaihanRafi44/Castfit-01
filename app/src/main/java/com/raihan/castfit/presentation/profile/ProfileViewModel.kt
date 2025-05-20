package com.raihan.castfit.presentation.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.raihan.castfit.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers

class ProfileViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {
    val isEditMode = MutableLiveData(false)
    val currentFullName = MutableLiveData<String>()
    val editedFullName = MutableLiveData<String>()
    private var originalFullName: String = ""

    fun doLogout() = userRepository.doLogout()

    fun updateProfileName(fullName: String) =
        userRepository.updateProfile(fullName = fullName)
            .asLiveData(Dispatchers.IO)

    fun getCurrentUser() = userRepository.getCurrentUser()

    fun createChangePwdRequest() {
        userRepository.requestChangePasswordByEmail()
    }

    fun changeEditMode(isCancel: Boolean = false) {
        val currentValue = isEditMode.value ?: false
        isEditMode.postValue(!currentValue)

        if (!currentValue) {
            // masuk mode edit
            originalFullName = currentFullName.value.orEmpty()
        } else if (isCancel) {
            // keluar mode edit dan user membatalkan
            editedFullName.value = originalFullName
        }
    }

    fun loadCurrentUser() {
        val user = userRepository.getCurrentUser()
        val name = user?.fullName.orEmpty()
        currentFullName.value = name
        editedFullName.value = name
        originalFullName = name
    }

    fun resetEditMode() {
        isEditMode.value = false
    }

    fun fetchDateOfBirthAndAge() = userRepository.getUserDateOfBirthAndAge()
        .asLiveData(Dispatchers.IO)

    fun saveDateOfBirthAndAge(dob: String, age: Int) = userRepository
        .updateUserDateOfBirthAndAge(dob, age)
        .asLiveData(Dispatchers.IO)

}
