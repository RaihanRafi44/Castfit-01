package com.raihan.castfit.data.source.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "schedule")
data class ScheduleActivityEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "user_id")
    var userId: String? = null,
    @ColumnInfo(name = "physical_activity_id")
    var physicalActivityId: String? = null,
    @ColumnInfo(name = "physical_activity_name")
    var physicalActivityName: String? = null,
    @ColumnInfo(name = "physical_activity_type")
    var physicalActivityType: String? = null,
    @ColumnInfo(name = "weather_status")
    var weatherStatus: String? = null,
    @ColumnInfo(name = "date_scheduled")
    var dateScheduled: String? = null
)