package com.raihan.castfit.data.source.network.model.weather

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DayRemote(
    @SerializedName("daily_chance_of_rain")
    val chanceOfRain: Int
)
