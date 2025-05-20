package com.raihan.castfit.data.datasource.progressactivity

import com.raihan.castfit.data.source.local.database.dao.ProgressActivityDao
import com.raihan.castfit.data.source.local.database.entity.ProgressActivityEntity
import kotlinx.coroutines.flow.Flow

interface ProgressActivityDataSource {
    fun getAllProgress(): Flow<List<ProgressActivityEntity>>

    suspend fun insertProgress(progress: ProgressActivityEntity): Long

    suspend fun updateProgress(progress: ProgressActivityEntity): Int

    suspend fun deleteProgress(progress: ProgressActivityEntity): Int

    suspend fun deleteAll()
}

class ProgressActivityDataSourceImpl(
    private val dao: ProgressActivityDao
) : ProgressActivityDataSource {
    override fun getAllProgress(): Flow<List<ProgressActivityEntity>> = dao.getAllProgress()

    override suspend fun insertProgress(progress: ProgressActivityEntity): Long = dao.insertProgress(progress)

    override suspend fun updateProgress(progress: ProgressActivityEntity): Int = dao.updateProgress(progress)

    override suspend fun deleteProgress(progress: ProgressActivityEntity): Int = dao.deleteProgress(progress)

    override suspend fun deleteAll() = dao.deleteAll()
}