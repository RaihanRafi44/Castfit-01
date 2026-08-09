package com.raihan.castfit.data.source.local.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.raihan.castfit.data.source.local.database.dao.HistoryActivityDao
import com.raihan.castfit.data.source.local.database.dao.ProgressActivityDao
import com.raihan.castfit.data.source.local.database.dao.ScheduleActivityDao
import com.raihan.castfit.data.source.local.database.entity.HistoryActivityEntity
import com.raihan.castfit.data.source.local.database.entity.ProgressActivityEntity
import com.raihan.castfit.data.source.local.database.entity.ScheduleActivityEntity

@Database(
    entities = [ProgressActivityEntity::class, HistoryActivityEntity::class, ScheduleActivityEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun progressActivityDao(): ProgressActivityDao

    abstract fun historyActivityDao(): HistoryActivityDao

    abstract fun scheduleActivityDao(): ScheduleActivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Konversi durasi dari menit ke detik untuk data yang sudah ada
                database.execSQL("UPDATE history SET duration = duration * 60 WHERE duration IS NOT NULL")
            }
        }

        fun createInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Log.d("Database", "Creating new database instance")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "Castfit.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

