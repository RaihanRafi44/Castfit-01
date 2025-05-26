package com.raihan.castfit.data.datasource.schedule

import com.raihan.castfit.data.source.local.database.dao.ScheduleActivityDao
import com.raihan.castfit.data.source.local.database.entity.HistoryActivityEntity
import com.raihan.castfit.data.source.local.database.entity.ScheduleActivityEntity
import kotlinx.coroutines.flow.Flow


interface ScheduleActivityDataSource{

    fun getUserSchedule(userId: String): Flow<List<ScheduleActivityEntity>>

    fun getAllSchedule(): Flow<List<ScheduleActivityEntity>>

    suspend fun insertSchedule(schedule: ScheduleActivityEntity): Long

    suspend fun updateSchedule(schedule: ScheduleActivityEntity): Int

    suspend fun deleteSchedule(schedule: ScheduleActivityEntity): Int

    suspend fun deleteAll()

}

class ScheduleActivityDataSourceImpl(

    private val dao: ScheduleActivityDao

) : ScheduleActivityDataSource {

    override fun getUserSchedule(userId: String): Flow<List<ScheduleActivityEntity>> = dao.getUserSchedule(userId)

    override fun getAllSchedule(): Flow<List<ScheduleActivityEntity>> = dao.getAllSchedule()

    override suspend fun insertSchedule(schedule: ScheduleActivityEntity): Long = dao.insertSchedule(schedule)

    override suspend fun updateSchedule(schedule: ScheduleActivityEntity): Int = dao.updateSchedule(schedule)

    override suspend fun deleteSchedule(schedule: ScheduleActivityEntity): Int = dao.deleteSchedule(schedule)

    override suspend fun deleteAll() = dao.deleteAll()
}