package com.raihan.castfit.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.raihan.castfit.data.datasource.progressactivity.ProgressActivityDataSource
import com.raihan.castfit.data.mapper.toProgressActivityEntity
import com.raihan.castfit.data.mapper.toProgressActivityList
import com.raihan.castfit.data.model.PhysicalActivity
import com.raihan.castfit.data.model.ProgressActivity
import com.raihan.castfit.data.model.User
import com.raihan.castfit.data.source.local.database.entity.ProgressActivityEntity
import com.raihan.castfit.utils.ResultWrapper
import com.raihan.castfit.utils.proceed
import com.raihan.castfit.utils.proceedFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

interface ProgressActivityRepository {
    fun getUserProgressData(): Flow<ResultWrapper<List<ProgressActivity>>>

    fun getProgress(userId: String): Flow<List<ProgressActivityEntity>>

    fun createProgress(
        activity: PhysicalActivity,
        user: User,
        dateStarted: String,
        startedAt: String,
    ): Flow<ResultWrapper<Boolean>>

    fun deleteProgress(progress: ProgressActivity): Flow<ResultWrapper<Boolean>>

    suspend fun deleteAll() : Flow<ResultWrapper<Boolean>>

    fun createProgressByEntity(progress: ProgressActivity): Flow<ResultWrapper<Boolean>>
}

class ProgressActivityRepositoryImpl(
    private val dataSource: ProgressActivityDataSource,
    private val firebaseAuth: FirebaseAuth
) : ProgressActivityRepository {

    override fun getUserProgressData(): Flow<ResultWrapper<List<ProgressActivity>>> {
        // Get current user ID
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId == null) {
            Log.d("GetProgress", "No user logged in, returning empty list")
            return kotlinx.coroutines.flow.flowOf(ResultWrapper.Empty(emptyList()))
        }

        Log.d("GetProgress", "Getting progress for user: $currentUserId")

        return dataSource.getUserProgress(currentUserId)  // Changed from getAllProgress to getUserProgress
            .map {
                Log.d("GetProgress", "Jumlah data DAO untuk user $currentUserId = ${it.size}")
                proceed {
                    it.toProgressActivityList().also { list ->
                        Log.d("GetProgress", "Converted to progress list: ${list.size} items")
                        list.forEach { progress ->
                            Log.d("GetProgress", "Progress ID: ${progress.id}, Name: ${progress.physicalActivityName}, UserId: ${progress.userId}")
                        }
                    }
                }
            }
            .map {
                if (it.payload?.isEmpty() == true) {
                    Log.d("GetProgress", "Returning empty result wrapper")
                    ResultWrapper.Empty(it.payload)
                } else {
                    Log.d("GetProgress", "Returning success result wrapper with ${it.payload?.size ?: 0} items")
                    it
                }
            }
            .catch { e ->
                Log.e("GetProgress", "Error getting progress data", e)
                emit(ResultWrapper.Error(Exception(e)))
            }
            .onStart {
                Log.d("GetProgress", "Starting to get progress data for user: $currentUserId")
                emit(ResultWrapper.Loading())
            }
    }

    override fun getProgress(userId: String): Flow<List<ProgressActivityEntity>> {
        return dataSource.getUserProgress(userId)
    }

    override fun createProgress(
        activity: PhysicalActivity,
        user: User,
        dateStarted: String,
        startedAt: String,
    ): Flow<ResultWrapper<Boolean>> {
        return proceedFlow {
            if (user.id.isEmpty()) {
                Log.e("CreateProgress", "Cannot create progress: user ID is empty")
                throw IllegalStateException("User ID is empty")
            }

            val entity = ProgressActivityEntity(
                id = null,
                userId = user.id,
                physicalActivityId = activity.id,
                physicalActivityName = activity.name,
                dateStarted = dateStarted,
                startedAt = startedAt
            )

            Log.d("CreateProgress", "Inserting progress: $entity")
            val insertedId = dataSource.insertProgress(entity)
            Log.d("CreateProgress", "Progress inserted with ID = $insertedId")

            insertedId > 0L // Return true jika penyisipan berhasil
        }
    }

    override fun deleteProgress(progress: ProgressActivity): Flow<ResultWrapper<Boolean>> {
        return proceedFlow {
            // Validasi sebelum delete
            if (progress.id == null || progress.id <= 0) {
                Log.e("DeleteProgress", "Cannot delete: Invalid progress ID (${progress.id})")
                throw IllegalArgumentException("Invalid progress ID")
            }

            // Validasi user
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId != progress.userId) {
                Log.e("DeleteProgress", "Cannot delete: User mismatch (current: $currentUserId, progress: ${progress.userId})")
                throw IllegalArgumentException("User not authorized to delete this progress")
            }

            Log.d("DeleteProgress", "Deleting progress with ID: ${progress.id}, Name: ${progress.physicalActivityName}")

            val entity = progress.toProgressActivityEntity()
            Log.d("DeleteProgress", "Entity to delete: $entity")

            val result = dataSource.deleteProgress(entity)
            Log.d("DeleteProgress", "Delete result: $result")

            if (result > 0) {
                Log.d("DeleteProgress", "Successfully deleted progress with ID: ${progress.id}")
                true
            } else {
                Log.w("DeleteProgress", "No rows affected when deleting progress with ID: ${progress.id}")
                false
            }
        }
    }

    override suspend fun deleteAll(): Flow<ResultWrapper<Boolean>> {
        return proceedFlow {
            dataSource.deleteAll()
            true
        }
    }

    override fun createProgressByEntity(progress: ProgressActivity): Flow<ResultWrapper<Boolean>> {
        return proceedFlow {

            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId.isNullOrEmpty()) {
                Log.e("CreateProgress", "Cannot create progress: User not logged in")
                throw IllegalStateException("User not logged in")
            }

            // Validasi input
            if (progress.physicalActivityName.isNullOrBlank()) {
                Log.e("CreateProgress", "Cannot create progress: Activity name is blank")
                throw IllegalArgumentException("Activity name cannot be blank")
            }

            if (progress.dateStarted.isNullOrBlank()) {
                Log.e("CreateProgress", "Cannot create progress: Date started is blank")
                throw IllegalArgumentException("Date started cannot be blank")
            }

            if (progress.startedAt.isNullOrBlank()) {
                Log.e("CreateProgress", "Cannot create progress: Started time is blank")
                throw IllegalArgumentException("Started time cannot be blank")
            }

            val entity = ProgressActivityEntity(
                id = null,
                userId = currentUserId,
                physicalActivityId = progress.physicalActivityId,
                physicalActivityName = progress.physicalActivityName,
                dateStarted = progress.dateStarted,
                startedAt = progress.startedAt
            )

            Log.d("CreateProgress", "Inserting progress: $entity")
            val insertedId = dataSource.insertProgress(entity)
            Log.d("CreateProgress", "Progress inserted with ID = $insertedId")

            insertedId > 0L // Return true jika penyisipan berhasil
        }
    }
}
