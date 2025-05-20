package com.raihan.castfit.data.repository

import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.raihan.castfit.data.datasource.location.LocationDataSource
import com.raihan.castfit.data.model.Location

class LocationRepository (private val dataSources: LocationDataSource) {

    fun getCurrentLocation(
        fusedLocationProviderClient: FusedLocationProviderClient,
        onSuccess: (Location) -> Unit,
        onFailure: () -> Unit
    ) {
        dataSources.getCurrentLocation(fusedLocationProviderClient, onSuccess, onFailure)
    }

    fun updateAddress(model: Location, geocoder: Geocoder): Location {
        return dataSources.resolveAddress(model, geocoder)
    }

    fun saveLocation(model: Location) {
        dataSources.saveCurrentLocation(model)
    }

    fun getSavedLocation(): Location? {
        return dataSources.getSavedLocation()
    }
}