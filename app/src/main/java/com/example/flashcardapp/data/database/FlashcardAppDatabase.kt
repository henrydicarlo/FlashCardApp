package com.example.flashcardapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flashcardapp.data.dao.DeckDao
import com.example.flashcardapp.data.dao.FlashcardDao
import com.example.flashcardapp.data.dao.LocationDao
import com.example.flashcardapp.data.dao.StudyInfoDao
import com.example.flashcardapp.data.dao.UserStatsDao
import com.example.flashcardapp.data.entities.Deck
import com.example.flashcardapp.data.entities.Flashcard
import com.example.flashcardapp.data.entities.StudyInfo
import com.example.flashcardapp.data.entities.StudyLocation
import com.example.flashcardapp.data.entities.UserStats

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