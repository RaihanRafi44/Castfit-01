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
        // Nama file SharedPreferences untuk menyimpan data lokasi
        private const val PREF_NAME = "WeatherAppPref"

        // Kunci untuk menyimpan data lokasi terkini dalam SharedPreferences
        private const val KEY_CURRENT_LOCATION = "currentLocation"
    }

    // Inisialisasi SharedPreferences
    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // Menyimpan data lokasi saat ini ke SharedPreferences dalam bentuk JSON
    fun saveCurrentLocation(entity: Location) {
        val json = gson.toJson(entity)
        sharedPreferences.edit {
            putString(KEY_CURRENT_LOCATION, json)
        }
    }

    /**
     * Mengambil data lokasi yang terakhir disimpan dari SharedPreferences
     * dan mengembalikannya dalam bentuk objek Location
     */
    fun getSavedLocation(): Location? {
        return sharedPreferences.getString(KEY_CURRENT_LOCATION, null)?.let { json ->
            gson.fromJson(json, Location::class.java)
        }
    }

    // --- Location Service Remote ---
    /**
     * Mengambil lokasi terkini dari perangkat menggunakan FusedLocationProviderClient
     * Jika berhasil, memanggil onSuccess dengan data lokasi (tanpa alamat)
     * Jika gagal, memanggil onFailure
     */
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

            // Lokasi berhasil diambil, buat objek Location dengan data latitude dan longitude
            onSuccess(
                Location(
                    date = getCurrentDate(),
                    location = "Fetching...", // Alamat belum diketahui, hanya koordinat
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            )
        }.addOnFailureListener {
            // Gagal mengambil lokasi
            onFailure() }
    }

    /**
     * Mengubah koordinat latitude dan longitude menjadi alamat yang bisa dibaca manusia
     * seperti "Bandung, Jawa Barat, Indonesia"
     */
    @Suppress("DEPRECATION")
    fun resolveAddress(location: Location, geocoder: Geocoder): Location {
        val lat = location.latitude ?: return location
        val lng = location.longitude ?: return location

        // Mengambil alamat dari koordinat (jika tersedia)
        val address = geocoder.getFromLocation(lat, lng, 1)?.firstOrNull() ?: return location

        // Format alamat menjadi satu baris
        val formatted = listOfNotNull(
            address.locality,
            address.adminArea,
            address.countryName
        ).joinToString(", ")

        // Mengembalikan data Location yang diperbarui dengan alamat lengkap
        return location.copy(location = formatted)
    }

    /**
     * Mengembalikan tanggal saat ini dalam format "dd MMMM yyyy" (contoh: 29 Juli 2025)
     */
    private fun getCurrentDate(): String {
        val locale = Locale("id", "ID")
        val formatter = SimpleDateFormat("dd MMMM yyyy", locale)
        return formatter.format(Date())
    }
}