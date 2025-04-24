package com.example.flashcardapp.ui.model

import com.example.flashcardapp.data.entities.StudyLocation

/**
 * Estado da UI para localizações
 */
data class LocationsUiState(
    val locations: List<StudyLocation> = emptyList(),
    val currentLocation: StudyLocation? = null,
    val canAddMore: Boolean = true,
    val isLoading: Boolean = true
)