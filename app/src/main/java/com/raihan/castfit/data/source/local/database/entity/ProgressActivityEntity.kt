package com.raihan.castfit.data.source.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress")
data class ProgressActivityEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "user_id")
    var userId: String? = null,
    @ColumnInfo(name = "physical_id")
    var physicalActivityId: String? = null,
    @ColumnInfo(name = "physical_name")
    var physicalActivityName: String? = null,
    @ColumnInfo(name = "date_started")
    var dateStarted: String? = null,
    @ColumnInfo(name = "started_at")
    var startedAt: String? = null,
)