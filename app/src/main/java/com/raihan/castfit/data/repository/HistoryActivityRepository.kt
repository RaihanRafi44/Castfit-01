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
            Log.d("GetHistory", "No user logged in, returning empty list")
            return kotlinx.coroutines.flow.flowOf(ResultWrapper.Empty(emptyList()))
        }

        Log.d("GetHistory", "Getting history for user: $currentUserId")

        return dataSource.getUserHistory(currentUserId)
            .map {
                Log.d("GetHistory", "Jumlah data DAO history untuk user $currentUserId = ${it.size}")
                proceed {
                    it.toHistoryActivityList().also { list ->
                        Log.d("GetHistory", "Converted to history list: ${list.size} items")
                        list.forEach { history ->
                            Log.d("GetHistory", "History ID: ${history.id}, Name: ${history.physicalActivityName}, UserId: ${history.userId}")
                        }
                    }
                }
            }
            .map {
                if (it.payload?.isEmpty() == true) {
                    Log.d("GetHistory", "Returning empty result wrapper")
                    ResultWrapper.Empty(it.payload)
                } else {
                    Log.d("GetHistory", "Returning success result wrapper with ${it.payload?.size ?: 0} items")
                    it
                }
            }
            .catch { e ->
                Log.e("GetHistory", "Error getting history data", e)
                emit(ResultWrapper.Error(Exception(e)))
            }
            .onStart {
                Log.d("GetHistory", "Starting to get history data for user: $currentUserId")
                emit(ResultWrapper.Loading())
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
                Log.e("CreateHistory", "Cannot create history: Invalid progress ID (${progress.id})")
                throw IllegalArgumentException("Invalid progress ID")
            }

            // Validasi user ownership
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId.isNullOrEmpty()) {
                Log.e("CreateHistory", "Cannot create history: User not logged in")
                throw IllegalArgumentException("User not logged in")
            }

            if (currentUserId != progress.userId) {
                Log.e("CreateHistory", "Cannot create history: User mismatch (current: $currentUserId, progress: ${progress.userId})")
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

            Log.d("CreateHistory", "Creating history: $historyEntity")
            val insertedId = dataSource.insertHistory(historyEntity)
            Log.d("CreateHistory", "History inserted with ID = $insertedId")
            Log.d("HistoryRepository", "Received duration: $duration")

            insertedId > 0L // Return true if insertion was successful
        }
    }

    override fun deleteHistory(history: HistoryActivity): Flow<ResultWrapper<Boolean>> {
        return proceedFlow {
            // Validasi sebelum delete
            if (history.id == null || history.id <= 0) {
                Log.e("DeleteHistory", "Cannot delete: Invalid history ID (${history.id})")
                throw IllegalArgumentException("Invalid history ID")
            }

            // Validasi user ownership
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("DeleteHistory", "Cannot delete: User not logged in")
                throw IllegalArgumentException("User not logged in")
            }

            // FIXED: Validasi menggunakan userId dari history langsung
            if (currentUserId != history.userId) {
                Log.e("DeleteHistory", "Cannot delete: User mismatch (current: $currentUserId, history: ${history.userId})")
                throw IllegalArgumentException("User not authorized to delete this history")
            }

            Log.d("DeleteHistory", "Deleting history with ID: ${history.id}")

            val entity = history.toHistoryActivityEntity()
            Log.d("DeleteHistory", "Entity to delete: $entity")

            val result = dataSource.deleteHistory(entity)
            Log.d("DeleteHistory", "Delete result: $result")

            if (result > 0) {
                Log.d("DeleteHistory", "Successfully deleted history with ID: ${history.id}")
                true
            } else {
                Log.w("DeleteHistory", "No rows affected when deleting history with ID: ${history.id}")
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