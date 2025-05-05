package com.example.flashcardapp.data.dao

import androidx.room.*
import com.example.flashcardapp.data.entities.Deck
import kotlinx.coroutines.flow.Flow

/**
 * DAO para decks
 */
@Dao
interface DeckDao {
    @Query("SELECT * FROM decks ORDER BY name")
    fun getAllDecks(): Flow<List<Deck>>

    @Query("SELECT * FROM decks WHERE deckId = :deckId")
    suspend fun getDeckById(deckId: Long): Deck?

    @Insert
    suspend fun insertDeck(deck: Deck): Long

    @Update
    suspend fun updateDeck(deck: Deck)

    @Delete
    suspend fun deleteDeck(deck: Deck)

    @Query("SELECT COUNT(*) FROM flashcards WHERE deck_id = :deckId")
    fun getCardCountForDeck(deckId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM flashcards INNER JOIN study_info ON flashcards.flashcardId = study_info.flashcard_id WHERE deck_id = :deckId AND study_info.nextReviewDate <= :currentTime")
    fun getDueCardCountForDeck(deckId: Long, currentTime: Long = System.currentTimeMillis()): Flow<Int>

    @Query("SELECT * FROM decks ORDER BY name")
    fun getAll(): List<Deck>

    @Query("DELETE FROM flashcards")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(decks: List<Deck>)
}