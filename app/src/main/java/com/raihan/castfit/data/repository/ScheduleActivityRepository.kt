package com.raihan.castfit.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.raihan.castfit.data.datasource.schedule.ScheduleActivityDataSource
import com.raihan.castfit.data.mapper.toHistoryActivityList
import com.raihan.castfit.data.mapper.toScheduleActivityEntity
import com.raihan.castfit.data.mapper.toScheduleActivityList
import com.raihan.castfit.data.model.PhysicalActivity
import com.raihan.castfit.data.model.ScheduleActivity
import com.raihan.castfit.data.model.User
import com.raihan.castfit.data.source.local.database.entity.ScheduleActivityEntity
import com.raihan.castfit.utils.ResultWrapper
import com.raihan.castfit.utils.proceed
import com.raihan.castfit.utils.proceedFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart


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
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId == null) {
            return kotlinx.coroutines.flow.flowOf(ResultWrapper.Empty(emptyList()))
        }

        return dataSource.getUserSchedule(currentUserId)
            .map {
                proceed {
                    it.toScheduleActivityList().also { list ->
                        list.forEach { schedule ->
                            Log.d("GetScheduled", "Schedule get")
                        }
                    }
                }
            }
            .map {
                if (it.payload?.isEmpty() == true) {
                    Log.d("GetScheduled", "Returning empty result wrapper")
                    ResultWrapper.Empty(it.payload)
                } else {
                    it
                }
            }
            .catch { e ->
                emit(ResultWrapper.Error(Exception(e)))
            }
            .onStart {
                emit(ResultWrapper.Loading())
            }
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
        return proceedFlow {
            // Validasi user ownership
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId.isNullOrEmpty()) {
                throw IllegalArgumentException("User not logged in")
            }

            // Validasi input
            if (activity.name.isBlank()) {
                throw IllegalArgumentException("Activity name cannot be blank")
            }

            if (dateScheduled.isBlank()) {
                throw IllegalArgumentException("Date scheduled cannot be blank")
            }

            val scheduleEntity = ScheduleActivityEntity(
                userId = currentUserId,
                physicalActivityId = activity.id,
                physicalActivityName = activity.name,
                physicalActivityType = activity.type,
                weatherStatus = weatherStatus,
                dateScheduled = dateScheduled
            )

            val result = dataSource.insertSchedule(scheduleEntity)

            result > 0
    }
    }

    override fun deleteSchedule(schedule: ScheduleActivity): Flow<ResultWrapper<Boolean>> {
        return proceedFlow {
            if (schedule.id == null || schedule.id <= 0) {

                throw IllegalArgumentException("Invalid schedule ID")
            }

            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("DeleteSchedule", "Cannot delete: User not logged in")
                throw IllegalArgumentException("User not logged in")
            }

            if (currentUserId != schedule.userId) {
                throw IllegalArgumentException("User not authorized to delete this schedule")
            }

            val entity = schedule.toScheduleActivityEntity()
            val result = dataSource.deleteSchedule(entity)

            result > 0
        }
    }

    override fun deleteAll(): Flow<ResultWrapper<Boolean>> {
        return proceedFlow {
            dataSource.deleteAll()
            true
        }
    }
}