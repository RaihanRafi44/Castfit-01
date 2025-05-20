package com.raihan.castfit.data.source.network.model.weather

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ForecastHourRemote(
    @SerializedName("time")
    val time: String,
    @SerializedName("temp_c")
    val temperature: Float,
    @SerializedName("feelslike_c")
    val feelsLikeTemperature: Float,
    @SerializedName("condition")
    val condition: WeatherConditionRemote,
)
