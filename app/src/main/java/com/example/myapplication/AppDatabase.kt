package com.example.myapplication

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WorkData::class, User::class, Person::class ,ShiftEndReportData::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workDao(): WorkDao
    abstract fun userDao(): UserDao
    abstract fun personDao(): PersonDao
    abstract fun shiftEndReportDao(): ShiftEndReportDao


    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // ✅ FIX
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}