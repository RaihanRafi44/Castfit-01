package com.raihan.castfit.presentation.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import coil.load
import com.google.android.gms.location.LocationServices
import com.raihan.castfit.R
import com.raihan.castfit.databinding.FragmentHomeBinding
import com.raihan.castfit.presentation.chartshistory.ChartsHistoryActivity
import com.raihan.castfit.presentation.recommendation.RecommendationActivity
import com.raihan.castfit.presentation.schedule.ScheduleActivity
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    //private val homeViewModel: HomeViewModel by viewModel()
    private val homeViewModel: HomeViewModel by activityViewModel()

    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private val geocoder by lazy { Geocoder(requireContext()) }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private var isCheckingProfile = false

    private var lastClickedButton: String? = null

    override fun onResume() {
        super.onResume()
        showUserData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        if (isLocationPermissionGranted()) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }
        showUserData()
        observeCurrentLocation()
        homeViewModel.loadSavedLocation()
        observeWeather()
        binding.swipeRefreshLayout.isEnabled = false
        homeViewModel.isProfileComplete.observe(viewLifecycleOwner) { isComplete ->
            if (!isCheckingProfile) return@observe

            if (isComplete == true) {
                when (lastClickedButton) {
                    "search" -> {
                        val intent = Intent(requireContext(), RecommendationActivity::class.java)
                        val weatherCondition = binding.homeWeather.textWeatherStatusHome.text.toString()
                        intent.putExtra("weatherCondition", weatherCondition)
                        startActivity(intent)
                    }
                    "schedule" -> {
                        val intent = Intent(requireContext(), ScheduleActivity::class.java)
                        startActivity(intent)
                    }
                }
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle("Lengkapi Profil")
                    .setMessage("Silakan lengkapi tanggal lahir di halaman profil terlebih dahulu untuk menggunakan fitur ini.")
                    .setPositiveButton("Oke", null)
                    .show()
            }

            // Reset flag
            isCheckingProfile = false
            lastClickedButton = null
        }


        binding.homeWeather.btnSearchActivity.setOnClickListener {
            val weatherData = homeViewModel.weather.value?.data
            val isWeatherAvailable = weatherData?.location != null && weatherData.current != null

            if (!isWeatherAvailable) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Data Cuaca Belum Tersedia")
                    .setMessage("Silakan tekan tombol 'Deteksi Lokasi' dan tunggu hingga data cuaca muncul.")
                    .setPositiveButton("Oke", null)
                    .show()
                return@setOnClickListener
            }

            if (!isCheckingProfile) {
                isCheckingProfile = true
                lastClickedButton = "search"
                homeViewModel.checkUserProfileComplete()
            }
        }


        binding.btnChartsUserActivity.setOnClickListener {
            val intent = Intent(requireContext(), ChartsHistoryActivity::class.java)
            startActivity(intent)
        }

        binding.btnScheduleHome.setOnClickListener {
            if (!isCheckingProfile) {
                isCheckingProfile = true
                lastClickedButton = "schedule"
                homeViewModel.checkUserProfileComplete()
            }
        }


    }

    private fun showUserData() {
        homeViewModel.getCurrentUser()?.let { user ->
            binding.homeProfile.textUsernameHome.text =
                getString(R.string.text_username_login, user.fullName)
        }
    }

    private fun observeCurrentLocation() {
        homeViewModel.currentLocation.observe(viewLifecycleOwner) { uiState ->
            when {
                uiState.error != null -> {
                    Toast.makeText(requireContext(), uiState.error, Toast.LENGTH_SHORT).show()
                }
                uiState.currentLocation != null -> {
                    val locationName = uiState.currentLocation.location
                    binding.homeWeather.textCurrentLocation.text = locationName
                    binding.homeWeather.textCurrentDayDate.text = uiState.currentLocation.date
                }
            }
        }

        binding.btnCurrentLocation.setOnClickListener {
            binding.swipeRefreshLayout.isRefreshing = true
            proceedWithCurrentLocation()
        }
    }


    private fun getCurrentLocation() {
        homeViewModel.fetchLocation(fusedLocationProviderClient, geocoder)
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun proceedWithCurrentLocation(){
        if (isLocationPermissionGranted()) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun observeWeather() {
        homeViewModel.weather.observe(viewLifecycleOwner) { weather ->
            weather?.data?.current?.let { currentWeather ->
                binding.homeWeather.textTemperatureHome.text = "${currentWeather.temperature}°C"
                binding.homeWeather.textWeatherStatusHome.text = currentWeather.condition.text
                binding.homeWeather.textWindValue.text = "${currentWeather.wind} km/jam"
                binding.homeWeather.textRainValue.text = "${currentWeather.precipitation} mm"
                binding.homeWeather.imageWeatherIcon.load("https:${currentWeather.condition.icon}")
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}