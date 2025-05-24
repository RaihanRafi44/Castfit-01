package com.raihan.castfit.data.source.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryActivityEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "progress_id")
    var progressId: Int? = null,
    @ColumnInfo(name = "physical_activity_name")
    var physicalActivityName: String? = null,
    @ColumnInfo(name = "date_ended")
    var dateEnded: String? = null,
    @ColumnInfo(name = "completed_at")
    var completedAt: String? = null,
    @ColumnInfo(name = "duration")
    var duration: Int? = null,

)