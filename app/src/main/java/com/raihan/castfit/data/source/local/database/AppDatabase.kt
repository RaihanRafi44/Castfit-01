package com.raihan.castfit.data.source.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.raihan.castfit.data.source.local.database.dao.ProgressActivityDao
import com.raihan.castfit.data.source.local.database.entity.ProgressActivityEntity


/*
@Database(
    entities = [ProgressActivityEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun progressActivityDao(): ProgressActivityDao

    companion object {
        private const val DB_NAME = "Castfit.db"

        fun createInstance(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DB_NAME
            ).fallbackToDestructiveMigration().build()
        }
    }
}*/

@Database(
    entities = [ProgressActivityEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun progressActivityDao(): ProgressActivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun createInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "Castfit.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

