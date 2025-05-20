package com.raihan.castfit.data.repository

import android.util.Log
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

interface ProgressActivityRepository {
    fun getUserProgressData(): Flow<ResultWrapper<List<ProgressActivity>>>

    fun createProgress(
        activity: PhysicalActivity,
        user: User,
        dateStarted: String,
        startedAt: String,
    ): Flow<ResultWrapper<Boolean>>

    fun deleteProgress(progress: ProgressActivity): Flow<ResultWrapper<Boolean>>

    suspend fun deleteAll() : Flow<ResultWrapper<Boolean>>
}

class ProgressActivityRepositoryImpl(
    private val dataSource: ProgressActivityDataSource
) : ProgressActivityRepository {
    override fun getUserProgressData(): Flow<ResultWrapper<List<ProgressActivity>>> {
        return dataSource.getAllProgress()
            .map {
                // Tambahkan log ini

                Log.d("GetProgress", "Jumlah data DAO = ${it.size}")
                proceed {
                    it.toProgressActivityList()
                }
            }
            .map {
                if (it.payload?.isEmpty() == false) return@map it
                ResultWrapper.Empty(it.payload)
            }
            .catch {
                emit(ResultWrapper.Error(Exception(it)))
            }
            .onStart {
                emit(ResultWrapper.Loading())
            }
    }


    override fun createProgress(
        activity: PhysicalActivity,
        user: User,
        dateStarted: String,
        startedAt: String,
    ): Flow<ResultWrapper<Boolean>> {
        return proceedFlow {
            val result = dataSource.insertProgress(
                ProgressActivityEntity(
                    id = null, // Biarkan Room yang generate ID
                    userId = user.id,
                    physicalActivityId = activity.id,
                    physicalActivityName = activity.name,
                    dateStarted = dateStarted,
                    startedAt = startedAt
                )
            )
            Log.d("CreateProgress", "Hasil insert progress = $result")
            result != 0L
        }
    }


    override fun deleteProgress(progress: ProgressActivity): Flow<ResultWrapper<Boolean>> {
        return proceedFlow { dataSource.deleteProgress(progress.toProgressActivityEntity()) == 0 }
    }

    override suspend fun deleteAll() : Flow<ResultWrapper<Boolean>> {
        return proceedFlow {
            dataSource.deleteAll()
            true
        }
    }
}