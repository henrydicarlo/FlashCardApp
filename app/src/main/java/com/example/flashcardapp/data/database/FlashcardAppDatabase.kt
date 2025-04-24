package com.example.flashcardapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flashcardapp.data.dao.*
import com.example.flashcardapp.data.entities.*

@Database(
    entities = [
        UserStats::class,
        Deck::class,
        Flashcard::class,
        StudyInfo::class,
        StudyLocation::class
    ],
    version = 1
)
abstract class FlashcardAppDatabase : RoomDatabase() {
    abstract fun userStatsDao(): UserStatsDao
    abstract fun deckDao(): DeckDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun studyInfoDao(): StudyInfoDao
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var INSTANCE: FlashcardAppDatabase? = null

        fun getDatabase(context: Context): FlashcardAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlashcardAppDatabase::class.java,
                    "flashcard_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}