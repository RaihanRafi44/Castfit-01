package com.raihan.castfit.data.mapper

import com.raihan.castfit.data.model.ProgressActivity
import com.raihan.castfit.data.source.local.database.entity.ProgressActivityEntity

fun ProgressActivity?.toProgressActivityEntity() =
    ProgressActivityEntity(
        id = this?.id,
        userId = this?.userId.orEmpty(),
        physicalActivityId = this?.physicalActivityId.orEmpty(),
        physicalActivityName = this?.physicalActivityName.orEmpty(),
        dateStarted = this?.dateStarted.orEmpty(),
        startedAt = this?.startedAt.orEmpty()
    )

fun ProgressActivityEntity?.toProgressActivity() =
    ProgressActivity(
        id = this?.id,
        userId = this?.userId.orEmpty(),
        physicalActivityId = this?.physicalActivityId.orEmpty(),
        physicalActivityName = this?.physicalActivityName.orEmpty(),
        dateStarted = this?.dateStarted.orEmpty(),
        startedAt = this?.startedAt.orEmpty()

    )

fun List<ProgressActivityEntity>.toProgressActivityList() = this.map { it.toProgressActivity() }