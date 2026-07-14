package com.example.customercrm.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromOutcome(outcome: CallOutcome): String = outcome.name

    @TypeConverter
    fun toOutcome(value: String): CallOutcome = CallOutcome.valueOf(value)
}

@Database(
    entities = [Customer::class, CallLog::class, FollowUp::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun callLogDao(): CallLogDao
    abstract fun followUpDao(): FollowUpDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "customer_crm_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
