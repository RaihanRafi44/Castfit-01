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
            Log.d("GetSchedule", "No user logged in, returning empty list")
            return kotlinx.coroutines.flow.flowOf(ResultWrapper.Empty(emptyList()))
        }

        Log.d("GetSchedule", "Getting schedule for user: $currentUserId")

        return dataSource.getUserSchedule(currentUserId)
            .map {
                Log.d("GetHistory", "Jumlah data DAO schedule untuk user $currentUserId = ${it.size}")
                proceed {
                    it.toScheduleActivityList().also { list ->
                        Log.d("GetScheduled", "Converted to schedule list: ${list.size} items")
                        list.forEach { schedule ->
                            Log.d("GetScheduled", "Schedule ID: ${schedule.id}, Name: ${schedule.physicalActivityName}, UserId: ${schedule.userId}")
                        }
                    }
                }
            }
            .map {
                if (it.payload?.isEmpty() == true) {
                    Log.d("GetScheduled", "Returning empty result wrapper")
                    ResultWrapper.Empty(it.payload)
                } else {
                    Log.d("GetHistory", "Returning success result wrapper with ${it.payload?.size ?: 0} items")
                    it
                }
            }
            .catch { e ->
                Log.e("GetScheduled", "Error getting schedule data", e)
                emit(ResultWrapper.Error(Exception(e)))
            }
            .onStart {
                Log.d("GetScheduled", "Starting to get schedule data for user: $currentUserId")
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

            Log.d("CreateSchedule", "createSchedule called with:")
            Log.d("CreateSchedule", "- Activity: ${activity.name} (ID: ${activity.id})")
            Log.d("CreateSchedule", "- User: ${user.fullName} (ID: ${user.id})")
            Log.d("CreateSchedule", "- Date: $dateScheduled")
            Log.d("CreateSchedule", "- Weather: $weatherStatus")
            // Validasi user ownership
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId.isNullOrEmpty()) {
                Log.e("CreateSchedule", "Cannot create schedule: User not logged in")
                throw IllegalArgumentException("User not logged in")
            }

            // Validasi input
            if (activity.name.isBlank()) {
                Log.e("CreateSchedule", "Cannot create schedule: Activity name is blank")
                throw IllegalArgumentException("Activity name cannot be blank")
            }

            if (dateScheduled.isBlank()) {
                Log.e("CreateSchedule", "Cannot create schedule: Date scheduled is blank")
                throw IllegalArgumentException("Date scheduled cannot be blank")
            }

            Log.d("CreateSchedule", "Creating schedule - Activity: ${activity.name}, Date: $dateScheduled, User: $currentUserId")

            val scheduleEntity = ScheduleActivityEntity(
                userId = currentUserId,
                physicalActivityId = activity.id,
                physicalActivityName = activity.name,
                physicalActivityType = activity.type,
                weatherStatus = weatherStatus,
                dateScheduled = dateScheduled
            )

            val result = dataSource.insertSchedule(scheduleEntity)
            Log.d("CreateSchedule", "Schedule created with ID: $result")

            result > 0
    }
    }

    override fun deleteSchedule(schedule: ScheduleActivity): Flow<ResultWrapper<Boolean>> {
        return proceedFlow {
            if (schedule.id == null || schedule.id <= 0) {
                Log.e("DeleteSchedule", "Cannot delete: Invalid schedule ID (${schedule.id})")
                throw IllegalArgumentException("Invalid schedule ID")
            }

            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("DeleteSchedule", "Cannot delete: User not logged in")
                throw IllegalArgumentException("User not logged in")
            }

            if (currentUserId != schedule.userId) {
                Log.e("DeleteSchedule", "Cannot delete: User mismatch (current: $currentUserId, schedule: ${schedule.userId})")
                throw IllegalArgumentException("User not authorized to delete this schedule")
            }

            Log.d("DeleteSchedule", "Deleting schedule with ID: ${schedule.id}")

            val entity = schedule.toScheduleActivityEntity()
            val result = dataSource.deleteSchedule(entity)

            Log.d("DeleteSchedule", "Delete result: $result")
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