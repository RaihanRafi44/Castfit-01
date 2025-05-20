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
    @Query("SELECT * FROM PROGRESS")
    fun getAllProgress(): Flow<List<ProgressActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressActivityEntity): Long

    @Update
    suspend fun updateProgress(progress: ProgressActivityEntity): Int

    @Delete
    suspend fun deleteProgress(progress: ProgressActivityEntity): Int

    @Query("DELETE FROM PROGRESS")
    suspend fun deleteAll()
}