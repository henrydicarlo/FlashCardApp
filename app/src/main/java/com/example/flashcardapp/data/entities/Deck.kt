package com.example.flashcardapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade de Deck/Baralho
 */
@Entity(tableName = "decks")
data class Deck(
    @PrimaryKey(autoGenerate = true) val deckId: Long = 0,
    val name: String,
    val description: String? = null,
    val creationDate: Long = System.currentTimeMillis()
)