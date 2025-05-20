package com.raihan.castfit.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Weather(
    val location: CurrentLocationRemote?,
    val current: CurrentWeather?
): Parcelable

@Parcelize
data class CurrentLocationRemote(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
): Parcelable

@Parcelize
data class CurrentWeather(
    val temperature: Float,
    val condition: WeatherCondition,
    val wind: Float,
    val precipitation: Float,
): Parcelable

@Parcelize
data class WeatherCondition(
    val text: String,
    val icon: String,
): Parcelable