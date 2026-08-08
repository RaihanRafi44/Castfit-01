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

        return dataSource.getUserProgress(currentUserId)  // Changed from getAllProgress to getUserProgress
            .map {
                proceed {
                    it.toProgressActivityList().also { list ->
                        list.forEach { progress ->
                            Log.d("GetProgress", "Progress get")
                        }
                    }
                }
            }
            .map {
                if (it.payload?.isEmpty() == true) {
                    Log.d("GetProgress", "Returning empty result wrapper")
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

            val insertedId = dataSource.insertProgress(entity)

            insertedId > 0L // Return true jika penyisipan berhasil
        }
    }

    override fun deleteProgress(progress: ProgressActivity): Flow<ResultWrapper<Boolean>> {
        return proceedFlow {
            // Validasi sebelum delete
            if (progress.id == null || progress.id <= 0) {
                throw IllegalArgumentException("Invalid progress ID")
            }

            // Validasi user
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId != progress.userId) {
                throw IllegalArgumentException("User not authorized to delete this progress")
            }

            val entity = progress.toProgressActivityEntity()

            val result = dataSource.deleteProgress(entity)

            if (result > 0) {
                true
            } else {
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
                throw IllegalStateException("User not logged in")
            }

            // Validasi input
            if (progress.physicalActivityName.isNullOrBlank()) {
                throw IllegalArgumentException("Activity name cannot be blank")
            }

            if (progress.dateStarted.isNullOrBlank()) {
                throw IllegalArgumentException("Date started cannot be blank")
            }

            if (progress.startedAt.isNullOrBlank()) {
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

            val insertedId = dataSource.insertProgress(entity)

            insertedId > 0L // Return true jika penyisipan berhasil
        }
    }
}
