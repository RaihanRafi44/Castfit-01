package com.raihan.castfit.data.datasource.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.core.content.edit
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.Gson
import com.raihan.castfit.data.model.Location
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationDataSource (context: Context, private val gson: Gson) {

    companion object {
        private const val PREF_NAME = "WeatherAppPref"
        private const val KEY_CURRENT_LOCATION = "currentLocation"
    }

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveCurrentLocation(entity: Location) {
        val json = gson.toJson(entity)
        sharedPreferences.edit {
            putString(KEY_CURRENT_LOCATION, json)
        }
    }

    fun getSavedLocation(): Location? {
        return sharedPreferences.getString(KEY_CURRENT_LOCATION, null)?.let { json ->
            gson.fromJson(json, Location::class.java)
        }
    }

    // --- Location Service Remote ---
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        fusedLocationProviderClient: FusedLocationProviderClient,
        onSuccess: (Location) -> Unit,
        onFailure: () -> Unit
    ) {
        fusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            location ?: return@addOnSuccessListener onFailure()
            onSuccess(
                Location(
                    date = getCurrentDate(),
                    location = "Fetching...",
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            )
        }.addOnFailureListener { onFailure() }
    }

    @Suppress("DEPRECATION")
    fun resolveAddress(location: Location, geocoder: Geocoder): Location {
        val lat = location.latitude ?: return location
        val lng = location.longitude ?: return location
        val address = geocoder.getFromLocation(lat, lng, 1)?.firstOrNull() ?: return location

        val formatted = listOfNotNull(
            address.locality,
            address.adminArea,
            address.countryName
        ).joinToString(", ")

        return location.copy(location = formatted)
    }

    private fun getCurrentDate(): String {
        val locale = Locale("id", "ID")
        val formatter = SimpleDateFormat("dd MMMM yyyy", locale)
        return formatter.format(Date())
    }
}