package com.raihan.castfit.utils

import java.text.SimpleDateFormat
import java.util.Locale

fun String.toReadableDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // sesuaikan dengan format dateEnded asli
        val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
        val date = inputFormat.parse(this)
        outputFormat.format(date ?: return this)
    } catch (e: Exception) {
        this
    }
}