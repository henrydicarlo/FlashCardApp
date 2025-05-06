package com.example.flashcardapp.ui.model

import kotlinx.serialization.Serializable

/**
 * Estado da UI para estatísticas do usuário
 */
@Serializable
data class UserStatsUiState(
    val correctAnswerRate: Float = 0f,
    val streakDays: Int = 0,
    val maxStreakDays: Int = 0,
    val totalStudyDays: Int = 0,
    val isLoading: Boolean = true
)