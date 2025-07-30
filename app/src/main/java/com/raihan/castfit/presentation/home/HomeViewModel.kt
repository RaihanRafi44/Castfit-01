package com.raihan.castfit.presentation.home

import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.raihan.castfit.data.model.Location
import com.raihan.castfit.data.model.Weather
import com.raihan.castfit.data.repository.LocationRepository
import com.raihan.castfit.data.repository.UserRepository
import com.raihan.castfit.data.repository.WeatherRepository
import com.raihan.castfit.utils.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    // State lokasi saat ini
    private val _currentLocation = MutableLiveData<CurrentLocationUiState>()
    val currentLocation: LiveData<CurrentLocationUiState> get() = _currentLocation

    // State cuaca saat ini
    private val _weather = MutableLiveData<WeatherUiState>()
    val weather: LiveData<WeatherUiState> get() = _weather

    // Status kelengkapan profil
    private val _isProfileComplete = MutableLiveData<Boolean>()
    val isProfileComplete: LiveData<Boolean> get() = _isProfileComplete

    // Mendapatkan data user saat ini
    fun getCurrentUser() = userRepository.getCurrentUser()

    // Ambil lokasi terkini dan simpan, lalu ambil data cuaca berdasarkan lokasi
    fun fetchLocation(
        fusedLocationProviderClient: FusedLocationProviderClient,
        geocoder: Geocoder
    ) {
        emitUiState(isLoading = true)
        locationRepository.getCurrentLocation(
            fusedLocationProviderClient = fusedLocationProviderClient,
            onSuccess = { location ->
                viewModelScope.launch(Dispatchers.IO) {
                    val resolved = locationRepository.updateAddress(location, geocoder)
                    locationRepository.saveLocation(resolved)
                    emitUiState(currentLocation = resolved)
                    fetchWeather(resolved.latitude, resolved.longitude)
                }
            },
            onFailure = {
                emitUiState(error = "Failed to get location")
            }
        )
    }

    // Muat lokasi tersimpan dan ambil data cuaca jika belum ada
    fun loadSavedLocation() {
        val savedLocation = locationRepository.getSavedLocation()
        emitUiState(currentLocation = savedLocation)

        // Jika lokasi ada dan data cuaca tidak tersedia, fetch weather
        savedLocation?.let { location ->
            if (location.latitude != null && location.longitude != null) {
                // Cek apakah data cuaca sudah tersedia
                val currentWeatherState = _weather.value
                if (currentWeatherState?.data == null && currentWeatherState?.isLoading != true) {
                    Log.d("HomeViewModel", "Location available but no weather data, fetching weather")
                    fetchWeather(location.latitude, location.longitude)
                }
            }
        }
    }

    fun refreshWeather() {
        val currentLocation = _currentLocation.value?.currentLocation
        if (currentLocation?.latitude != null && currentLocation.longitude != null) {
            Log.d("HomeViewModel", "Refreshing weather data for location: ${currentLocation.latitude}, ${currentLocation.longitude}")
            fetchWeather(currentLocation.latitude, currentLocation.longitude)
        } else {
            Log.w("HomeViewModel", "Cannot refresh weather: no location available")
            _weather.postValue(WeatherUiState(error = "Location not available for weather refresh"))
        }
    }

    // Ambil data cuaca dari repository menggunakan lat dan lon
    private fun fetchWeather(lat: Double?, lon: Double?) {
        if (lat == null || lon == null) return
        viewModelScope.launch {
            weatherRepository.getWeathers("$lat,$lon")
                .collect { result ->
                when (result) {
                    is ResultWrapper.Loading -> _weather.postValue(WeatherUiState(isLoading = true))
                    is ResultWrapper.Success -> _weather.postValue(
                        WeatherUiState(data = result.payload)
                    )
                    is ResultWrapper.Error -> _weather.postValue(
                        WeatherUiState(error = result.message ?: "Unknown error")
                    )
                    is ResultWrapper.Empty -> _weather.postValue(
                        WeatherUiState(error = "No data found")
                    )
                    is ResultWrapper.Idle -> {}
                }
            }
        }
    }

    // Update UI state untuk lokasi
    private fun emitUiState(
        isLoading: Boolean = false,
        currentLocation: Location? = null,
        error: String? = null
    ) {
        _currentLocation.postValue(CurrentLocationUiState(isLoading, currentLocation, error))
    }

    // Cek apakah profil user sudah lengkap (tanggal lahir dan usia)
    fun checkUserProfileComplete() {
        viewModelScope.launch {
            userRepository.getUserDateOfBirthAndAge().collect { result ->
                when (result) {
                    is ResultWrapper.Success -> {
                        val dob = result.payload?.first
                        val age = result.payload?.second
                        val isComplete = !dob.isNullOrEmpty() && age != null && age > 0
                        _isProfileComplete.postValue(isComplete)
                    }
                    is ResultWrapper.Error -> {
                        _isProfileComplete.postValue(false)
                    }
                    else -> {
                        //_isProfileComplete.postValue(false)
                    }
                }
            }
        }
    }

    // UI state untuk data cuaca
    data class WeatherUiState(
        val isLoading: Boolean = false,
        val data: Weather? = null,
        val error: String? = null
    )

    // UI state untuk data lokasi
    data class CurrentLocationUiState(
        val isLoading: Boolean,
        val currentLocation: Location?,
        val error: String?
    )
}