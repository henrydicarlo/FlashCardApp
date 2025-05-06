package com.example.flashcardapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Entidade de Deck/Baralho
 */
@Serializable
@Entity(tableName = "decks")
data class Deck(
    @PrimaryKey(autoGenerate = true) val deckId: Long = 0,
    val name: String,
    val description: String? = null,
    val creationDate: Long = System.currentTimeMillis()
)