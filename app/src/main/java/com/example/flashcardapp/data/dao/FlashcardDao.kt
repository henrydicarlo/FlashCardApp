package com.example.flashcardapp.data.dao

import androidx.room.*
import com.example.flashcardapp.data.entities.Flashcard
import kotlinx.coroutines.flow.Flow

/**
 * DAO para flashcards
 */
@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE deck_id = :deckId")
    fun getFlashcardsByDeck(deckId: Long): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE flashcardId = :flashcardId")
    suspend fun getFlashcardById(flashcardId: Long): Flashcard?

    @Query("SELECT f.* FROM flashcards f INNER JOIN study_info s ON f.flashcardId = s.flashcard_id WHERE f.deck_id = :deckId AND s.nextReviewDate <= :currentTime ORDER BY s.nextReviewDate")
    suspend fun getDueFlashcardsForDeck(deckId: Long, currentTime: Long = System.currentTimeMillis()): List<Flashcard>

    @Query("SELECT f.* FROM flashcards f INNER JOIN study_info s ON f.flashcardId = s.flashcard_id WHERE s.nextReviewDate <= :currentTime ORDER BY s.nextReviewDate")
    suspend fun getAllDueFlashcards(currentTime: Long = System.currentTimeMillis()): List<Flashcard>

    @Query("SELECT * FROM flashcards f INNER JOIN study_info s ON f.flashcardId = s.flashcard_id WHERE s.reviewLocations NOT LIKE '%' || :locationId || '%' ORDER BY RANDOM() LIMIT :limit")
    suspend fun getFlashcardsNotReviewedAtLocation(locationId: Long, limit: Int = 10): List<Flashcard>

    @Insert
    suspend fun insertFlashcard(flashcard: Flashcard): Long

    @Update
    suspend fun updateFlashcard(flashcard: Flashcard)

    @Delete
    suspend fun deleteFlashcard(flashcard: Flashcard)
}