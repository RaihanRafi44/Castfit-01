package com.raihan.castfit.data.source.network.model.weather

import androidx.annotation.Keep

@Keep
data class WeatherConditionRemote(
    val icon: String?,
    val text: String?
)
