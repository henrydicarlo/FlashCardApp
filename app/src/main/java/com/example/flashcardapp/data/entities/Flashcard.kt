package com.example.flashcardapp.data.entities

import androidx.room.*
import kotlinx.serialization.Serializable

/**
 * Entidade de Flashcard
 */
@Serializable
@Entity(tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = Deck::class,
            parentColumns = ["deckId"],
            childColumns = ["deck_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deck_id")]
)
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val flashcardId: Long = 0,
    @ColumnInfo(name = "deck_id") val deckId: Long,
    val type: FlashcardType,
    val question: String,
    val answer: String,
    // Para flashcards do tipo QUIZ, armazena opções separadas por |
    val options: String? = null,
    // Para flashcards do tipo CLOZE, pode armazenar o texto completo
    val fullText: String? = null,
    val creationDate: Long = System.currentTimeMillis()
)