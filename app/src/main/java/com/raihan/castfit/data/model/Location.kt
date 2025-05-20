package com.raihan.castfit.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    val date: String,
    val location: String,
    val latitude: Double?,
    val longitude: Double?
): Parcelable
