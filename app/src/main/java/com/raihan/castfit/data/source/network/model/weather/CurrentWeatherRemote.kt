package com.raihan.castfit.data.source.network.model.weather

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CurrentWeatherRemote(
    @SerializedName("temp_c")
    val temperature: Float?,
    @SerializedName("condition")
    val condition: WeatherConditionRemote?,
    @SerializedName("wind_kph")
    val wind: Float?,
    @SerializedName("precip_mm")
    val precipitation: Float?,

    )
