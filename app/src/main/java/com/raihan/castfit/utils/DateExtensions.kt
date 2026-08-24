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

fun Int.toReadableDuration(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    
    return when {
        hours > 0 -> "${hours} jam ${minutes} mnt ${seconds} dtk"
        minutes > 0 -> "${minutes} mnt ${seconds} dtk"
        else -> "${seconds} dtk"
    }
}
