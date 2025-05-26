package com.raihan.castfit.data.mapper

import com.raihan.castfit.data.source.local.database.entity.ScheduleActivityEntity
import com.raihan.castfit.data.model.ScheduleActivity
import com.raihan.castfit.data.source.local.database.entity.HistoryActivityEntity

fun ScheduleActivity?.toScheduleActivityEntity() =
    ScheduleActivityEntity(
        id = this?.id,
        userId = this?.userId,
        physicalActivityId = this?.physicalActivityId,
        physicalActivityName = this?.physicalActivityName.orEmpty(),
        physicalActivityType = this?.physicalActivityType.orEmpty(),
        weatherStatus = this?.weatherStatus.orEmpty(),
        dateScheduled = this?.dateScheduled.orEmpty()
    )

fun ScheduleActivityEntity.toScheduleActivity() =
    ScheduleActivity(
        id = this.id,
        userId = this.userId,
        physicalActivityId = this.physicalActivityId,
        physicalActivityName = this.physicalActivityName.orEmpty(),
        physicalActivityType = this.physicalActivityType.orEmpty(),
        weatherStatus = this.weatherStatus.orEmpty(),
        dateScheduled = this.dateScheduled.orEmpty()
    )

fun List<ScheduleActivityEntity>.toScheduleActivityList() = this.map { it.toScheduleActivity() }