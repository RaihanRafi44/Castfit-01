package com.raihan.castfit.data.datasource.historyactivity

import com.raihan.castfit.data.source.local.database.dao.HistoryActivityDao
import com.raihan.castfit.data.source.local.database.entity.HistoryActivityEntity
import com.raihan.castfit.data.source.local.database.entity.ProgressActivityEntity
import kotlinx.coroutines.flow.Flow

interface HistoryActivityDataSource {

    fun getUserHistory(userId: String): Flow<List<HistoryActivityEntity>>

    fun getAllHistory(): Flow<List<HistoryActivityEntity>>

    fun getUserHistoryByProgressId(progressId: Int): Flow<List<HistoryActivityEntity>>

    suspend fun insertHistory(history: HistoryActivityEntity): Long

    suspend fun updateHistory(history: HistoryActivityEntity): Int

    suspend fun deleteHistory(history: HistoryActivityEntity): Int

    suspend fun deleteAll()
}

class HistoryActivityDataSourceImpl(

    private val dao: HistoryActivityDao

) : HistoryActivityDataSource {

    override fun getAllHistory(): Flow<List<HistoryActivityEntity>> = dao.getAllHistory()

    override fun getUserHistory(userId: String): Flow<List<HistoryActivityEntity>> = dao.getUserHistory(userId)

    override fun getUserHistoryByProgressId(progressId: Int): Flow<List<HistoryActivityEntity>> = dao.getUserHistoryByProgressId(progressId)

    override suspend fun insertHistory(history: HistoryActivityEntity): Long = dao.insertHistory(history)

    override suspend fun updateHistory(history: HistoryActivityEntity): Int = dao.updateHistory(history)

    override suspend fun deleteHistory(history: HistoryActivityEntity): Int = dao.deleteHistory(history)

    override suspend fun deleteAll() = dao.deleteAll()
}