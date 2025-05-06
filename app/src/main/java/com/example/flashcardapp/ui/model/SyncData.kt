package com.example.flashcardapp.ui.model

import com.example.flashcardapp.data.entities.Deck
import com.example.flashcardapp.data.entities.Flashcard
import com.example.flashcardapp.data.entities.StudyInfo
import com.example.flashcardapp.data.entities.StudyLocation
import com.example.flashcardapp.data.entities.UserStats
import kotlinx.serialization.Serializable

@Serializable
data class SyncData(
    val decks: List<Deck>,
    val flashcards: List<Flashcard>,
    val studyInfos: List<StudyInfo>,
    val locations: List<StudyLocation>,
    val userStats: List<UserStats>
)
