package com.raihan.castfit.data.source.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.raihan.castfit.data.source.local.database.entity.HistoryActivityEntity
import com.raihan.castfit.data.source.local.database.entity.ProgressActivityEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface HistoryActivityDao {

    /*@Query("""
        SELECT h.* FROM history h 
        INNER JOIN progress p ON h.progress_id = p.id 
        WHERE p.user_id = :userId
    """)
    fun getUserHistory(userId: String): Flow<List<HistoryActivityEntity>>*/

    @Query("SELECT * FROM history WHERE user_id = :userId ORDER BY date_ended DESC, completed_at DESC")
    fun getUserHistory(userId: String): Flow<List<HistoryActivityEntity>>

    @Query("SELECT * FROM history")
    fun getAllHistory(): Flow<List<HistoryActivityEntity>>

    @Query("SELECT * FROM history WHERE progress_id = :progressId")
    fun getUserHistoryByProgressId(progressId: Int): Flow<List<HistoryActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryActivityEntity): Long

    @Update
    suspend fun updateHistory(history: HistoryActivityEntity): Int

    @Delete
    suspend fun deleteHistory(history: HistoryActivityEntity): Int

    @Query("DELETE FROM history")
    suspend fun deleteAll()
}