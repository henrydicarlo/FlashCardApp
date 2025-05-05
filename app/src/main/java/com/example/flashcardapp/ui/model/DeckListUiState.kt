package com.example.flashcardapp.ui.model

import com.example.flashcardapp.data.entities.Deck

/**
 * Estado da UI para listagem de baralhos
 */
data class DeckListUiState(
    val decks: List<DeckWithStats> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * Estado da UI para decks com estatísticas
 */
data class DeckWithStats(
    val deck: Deck,
    val cardCount: Int = 0,
    val dueCardCount: Int = 0
)