package com.raihan.castfit.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.raihan.castfit.data.datasource.schedule.ScheduleActivityDataSource
import com.raihan.castfit.data.model.PhysicalActivity
import com.raihan.castfit.data.model.ScheduleActivity
import com.raihan.castfit.data.model.User
import com.raihan.castfit.data.source.local.database.entity.ScheduleActivityEntity
import com.raihan.castfit.utils.ResultWrapper
import com.raihan.castfit.utils.proceedFlow
import kotlinx.coroutines.flow.Flow


interface ScheduleActivityRepository {
    fun getUserScheduleActivity(): Flow<ResultWrapper<List<ScheduleActivity>>>

    fun getSchedule(userId: String): Flow<List<ScheduleActivityEntity>>

    fun createSchedule(
        activity: PhysicalActivity,
        user: User,
        dateScheduled: String,
        weatherStatus: String,
    ): Flow<ResultWrapper<Boolean>>

    fun deleteSchedule(schedule: ScheduleActivity): Flow<ResultWrapper<Boolean>>

    fun deleteAll(): Flow<ResultWrapper<Boolean>>
}

class ScheduleActivityRepositoryImpl(

    private val dataSource: ScheduleActivityDataSource,
    private val firebaseAuth: FirebaseAuth

    ) : ScheduleActivityRepository {

    override fun getUserScheduleActivity(): Flow<ResultWrapper<List<ScheduleActivity>>> {
        TODO("Not yet implemented")
    }

    override fun getSchedule(userId: String): Flow<List<ScheduleActivityEntity>> {
        return dataSource.getUserSchedule(userId)
    }

    override fun createSchedule(
        activity: PhysicalActivity,
        user: User,
        dateScheduled: String,
        weatherStatus: String
    ): Flow<ResultWrapper<Boolean>> {
        TODO("Not yet implemented")
    }

    override fun deleteSchedule(schedule: ScheduleActivity): Flow<ResultWrapper<Boolean>> {
        TODO("Not yet implemented")
    }

    override fun deleteAll(): Flow<ResultWrapper<Boolean>> {
        return proceedFlow {
            dataSource.deleteAll()
            true
        }
    }
}