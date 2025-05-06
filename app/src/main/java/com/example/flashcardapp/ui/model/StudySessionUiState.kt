package com.example.flashcardapp.ui.model

import com.example.flashcardapp.data.entities.Flashcard
import com.example.flashcardapp.data.entities.StudyInfo
import com.example.flashcardapp.data.entities.StudyLocation


/**
 * Estado da UI para estudo de flashcards
 */
data class StudySessionUiState(
    val currentFlashcard: Flashcard? = null,
    val currentStudyInfo: StudyInfo? = null,
    val isAnswerRevealed: Boolean = false,
    val remainingCards: Int = 0,
    val isLoading: Boolean = true,
    val isCompleted: Boolean = false,
    val currentLocation: StudyLocation? = null
)