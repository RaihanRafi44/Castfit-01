package com.raihan.castfit.data.source.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.raihan.castfit.data.source.local.database.entity.ProgressActivityEntity
import com.raihan.castfit.data.source.local.database.entity.ScheduleActivityEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ScheduleActivityDao {

    @Query("SELECT * FROM schedule")
    fun getAllSchedule(): Flow<List<ScheduleActivityEntity>>

    @Query("SELECT * FROM schedule WHERE user_id = :userId")
    fun getUserSchedule(userId: String): Flow<List<ScheduleActivityEntity>>

    @Insert
    suspend fun insertSchedule(schedule: ScheduleActivityEntity): Long

    @Update
    suspend fun updateSchedule(schedule: ScheduleActivityEntity): Int

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleActivityEntity): Int

    @Query("DELETE FROM schedule")
    suspend fun deleteAll()

}