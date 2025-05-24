package com.raihan.castfit.data.mapper

import com.raihan.castfit.data.model.HistoryActivity
import com.raihan.castfit.data.model.ProgressActivity
import com.raihan.castfit.data.source.local.database.entity.HistoryActivityEntity
import com.raihan.castfit.data.source.local.database.entity.ProgressActivityEntity

fun HistoryActivity?.toHistoryActivityEntity() =
    HistoryActivityEntity(
        id = this?.id,
        progressId = this?.progressId,
        physicalActivityName = this?.physicalActivityName.orEmpty(),
        dateEnded = this?.dateEnded.orEmpty(),
        completedAt = this?.completedAt.orEmpty(),
        duration = this?.duration
    )

fun HistoryActivityEntity?.toHistoryActivity() =
    HistoryActivity(
        id = this?.id,
        progressId = this?.progressId,
        physicalActivityName = this?.physicalActivityName.orEmpty(),
        dateEnded = this?.dateEnded.orEmpty(),
        completedAt = this?.completedAt.orEmpty(),
        duration = this?.duration

    )

fun List<HistoryActivityEntity>.toHistoryActivityList() = this.map { it.toHistoryActivity() }