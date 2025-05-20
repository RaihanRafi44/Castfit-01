package com.raihan.castfit.utils

import java.util.Calendar

fun calculateAge(year: Int, month: Int, day: Int): Int {
    val today = Calendar.getInstance()
    val dob = Calendar.getInstance()
    dob.set(year, month, day)

    var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
        age--
    }
    return age
}
