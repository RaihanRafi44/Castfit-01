package com.raihan.castfit.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.raihan.castfit.data.datasource.historyactivity.HistoryActivityDataSource
import com.raihan.castfit.data.mapper.toHistoryActivityEntity
import com.raihan.castfit.data.mapper.toHistoryActivityList
import com.raihan.castfit.data.mapper.toProgressActivityEntity
import com.raihan.castfit.data.mapper.toProgressActivityList
import com.raihan.castfit.data.model.HistoryActivity
import com.raihan.castfit.data.model.PhysicalActivity
import com.raihan.castfit.data.model.ProgressActivity
import com.raihan.castfit.data.model.User
import com.raihan.castfit.data.source.local.database.entity.HistoryActivityEntity
import com.raihan.castfit.data.source.local.database.entity.ProgressActivityEntity
import com.raihan.castfit.utils.ResultWrapper
import com.raihan.castfit.utils.proceed
import com.raihan.castfit.utils.proceedFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

interface HistoryActivityRepository {

    fun getUserHistoryData(): Flow<ResultWrapper<List<HistoryActivity>>>

    fun getHistoryByProgressId(progressId: Int): Flow<List<HistoryActivityEntity>>

    fun createHistory(
        progress: ProgressActivity,
        dateEnded: String,
        completedAt: String,
        duration: Int
    ): Flow<ResultWrapper<Boolean>>

    fun deleteHistory(history: HistoryActivity): Flow<ResultWrapper<Boolean>>

    suspend fun deleteAll() : Flow<ResultWrapper<Boolean>>

}

class HistoryActivityRepositoryImpl(
    private val dataSource: HistoryActivityDataSource,
    private val firebaseAuth: FirebaseAuth
) : HistoryActivityRepository {

    override fun getUserHistoryData(): Flow<ResultWrapper<List<HistoryActivity>>> {

        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId == null) {
            return kotlinx.coroutines.flow.flowOf(ResultWrapper.Empty(emptyList()))
        }

        return dataSource.getUserHistory(currentUserId)
            .map {
                proceed {
                    it.toHistoryActivityList().also { list ->
                        list.forEach { history ->
                            Log.d("GetHistory", "History get")
                        }
                    }
                }
            }
            .map {
                if (it.payload?.isEmpty() == true) {
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
                delay(1000) // Artificial delay for shimmer
            }
    }

    override fun getHistoryByProgressId(progressId: Int): Flow<List<HistoryActivityEntity>> {
        return dataSource.getUserHistoryByProgressId(progressId)
    }

    override fun createHistory(
        progress: ProgressActivity,
        dateEnded: String,
        completedAt: String,
        duration: Int
    ): Flow<ResultWrapper<Boolean>> {
        return proceedFlow {
            // Validasi progress ID
            if (progress.id == null || progress.id <= 0) {
                throw IllegalArgumentException("Invalid progress ID")
            }

            // Validasi user ownership
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId.isNullOrEmpty()) {
                throw IllegalArgumentException("User not logged in")
            }

            if (currentUserId != progress.userId) {
                throw IllegalArgumentException("User not authorized to create history for this progress")
            }

            val historyEntity = HistoryActivityEntity(
                id = null,
                userId = currentUserId,
                progressId = progress.id,
                physicalActivityName = progress.physicalActivityName,
                dateEnded = dateEnded,
                completedAt = completedAt,
                duration = duration
            )

            val insertedId = dataSource.insertHistory(historyEntity)

            insertedId > 0L // Return true if insertion was successful
        }
    }

    override fun deleteHistory(history: HistoryActivity): Flow<ResultWrapper<Boolean>> {
        return proceedFlow {
            // Validasi sebelum delete
            if (history.id == null || history.id <= 0) {
                throw IllegalArgumentException("Invalid history ID")
            }

            // Validasi user ownership
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                throw IllegalArgumentException("User not logged in")
            }

            // FIXED: Validasi menggunakan userId dari history langsung
            if (currentUserId != history.userId) {
                throw IllegalArgumentException("User not authorized to delete this history")
            }

            val entity = history.toHistoryActivityEntity()

            val result = dataSource.deleteHistory(entity)

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
}