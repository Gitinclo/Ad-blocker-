package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FilterRuleEntity::class,
        CosmeticRuleEntity::class,
        WhitelistedSiteEntity::class,
        InterceptionLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AdBlockDatabase : RoomDatabase() {
    abstract fun adBlockDao(): AdBlockDao

    companion object {
        @Volatile
        private var INSTANCE: AdBlockDatabase? = null

        fun getInstance(context: Context): AdBlockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AdBlockDatabase::class.java,
                    "cleanbrowse_ai.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
