package com.raihan.castfit.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class PhysicalActivity(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String,
    val minAge : Int,
    val maxAge : Int,
    val physicalImg: Int
) : Parcelable
