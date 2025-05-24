package com.raihan.castfit.data.source.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.raihan.castfit.data.source.local.database.entity.ProgressActivityEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ProgressActivityDao {
    @Query("SELECT * FROM progress")
    fun getAllProgress(): Flow<List<ProgressActivityEntity>>

    @Query("SELECT * FROM progress WHERE user_id = :userId")
    fun getUserProgress(userId: String): Flow<List<ProgressActivityEntity>>

    @Insert
    suspend fun insertProgress(progress: ProgressActivityEntity): Long

    @Update
    suspend fun updateProgress(progress: ProgressActivityEntity): Int

    @Delete
    suspend fun deleteProgress(progress: ProgressActivityEntity): Int

    @Query("DELETE FROM progress")
    suspend fun deleteAll()
}