package com.raihan.castfit.data.repository

import android.util.Log
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

    fun getHistoryById(progressId: Int): Flow<List<HistoryActivityEntity>>

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
    private val dataSource: HistoryActivityDataSource
) : HistoryActivityRepository {
    override fun getUserHistoryData(): Flow<ResultWrapper<List<HistoryActivity>>> {
        return dataSource.getAllHistory()
            .map {
                Log.d("GetHistory", "Jumlah data DAO history = ${it.size}")
                proceed {
                    it.toHistoryActivityList().also { list ->
                        Log.d("GetHistory", "Converted to history list: ${list.size} items")
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
                Log.e("GetHistory", "Error getting progress data", e)
                emit(ResultWrapper.Error(Exception(e)))
            }
            .onStart {
                Log.d("GetHistory", "Starting to get progress data")
                emit(ResultWrapper.Loading())
            }
    }

    override fun getHistoryById(progressId: Int): Flow<List<HistoryActivityEntity>> {
        return dataSource.getUserHistory(progressId)
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

            val historyEntity = HistoryActivityEntity(
                id = null, // Auto-generate ID
                progressId = progress.id,
                physicalActivityName = progress.physicalActivityName, // Simpan nama aktivitas fisik
                dateEnded = dateEnded,
                completedAt = completedAt,
                duration = duration
            )

            Log.d("CreateHistory", "Creating history: $historyEntity")
            val insertedId = dataSource.insertHistory(historyEntity)
            Log.d("CreateHistory", "History inserted with ID = $insertedId")

            insertedId > 0L // Return true if insertion was successful
        }
    }

    /*override fun deleteHistory(history: HistoryActivity): Flow<ResultWrapper<Boolean>> {
        return proceedFlow { dataSource.deleteHistory(history.toHistoryActivityEntity()) > 0 }
    }*/

    override fun deleteHistory(history: HistoryActivity): Flow<ResultWrapper<Boolean>> {
        return proceedFlow {
            // Validasi sebelum delete
            if (history.id == null || history.id <= 0) {
                Log.e("DeleteHistory", "Cannot delete: Invalid history ID (${history.id})")
                throw IllegalArgumentException("Invalid history ID")
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