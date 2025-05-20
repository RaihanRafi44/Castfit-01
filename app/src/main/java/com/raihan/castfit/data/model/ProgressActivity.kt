package com.raihan.castfit.data.model

import java.util.UUID

data class ProgressActivity(
    val id: Int? = null,
    val userId: String,
    val physicalActivityId: String,
    val physicalActivityName: String,
    val dateStarted: String,
    val startedAt: String,
)
