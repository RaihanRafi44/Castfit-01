package com.raihan.castfit.data.source.network.model.weather

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LocationRemote(
    @SerializedName("name")
    val name: String?,
    @SerializedName("region")
    val region: String?,
    @SerializedName("country")
    val country: String?,
    @SerializedName("lat")
    val lat: Double?,
    @SerializedName("lon")
    val lon: Double?,
)
