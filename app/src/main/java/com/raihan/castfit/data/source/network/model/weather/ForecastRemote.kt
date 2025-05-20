package com.raihan.castfit.data.source.network.model.weather

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.raihan.castfit.data.source.network.model.weather.ForecastDayRemote

@Keep
data class ForecastRemote(
    @SerializedName("forecastday") val forecastDay: List<ForecastDayRemote>
)
