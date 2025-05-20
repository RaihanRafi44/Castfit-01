package com.raihan.castfit.data.source.network.model.weather

import androidx.annotation.Keep
import com.raihan.castfit.data.source.network.model.weather.CurrentWeatherRemote
import com.raihan.castfit.data.source.network.model.weather.LocationRemote

@Keep
data class WeatherData(
    val location: LocationRemote?,
    val current: CurrentWeatherRemote?,
)
