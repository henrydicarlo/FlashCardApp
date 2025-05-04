package com.example.flashcardapp.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiDeck(
    val id: String,
    val name: String,
    val description: String?,
    val creationDate: Long,
    val userId: String,
    val isPublic: Boolean = false
)

@Serializable
data class ApiFlashcard(
    val id: String,
    val deckId: String,
    val type: String,
    val question: String,
    val answer: String,
    val options: String? = null,
    val fullText: String? = null,
    val creationDate: Long
)

@Serializable
data class ApiStudyInfo(
    val flashcardId: String,
    val easeFactor: Double,
    val interval: Int,
    val repetitions: Int,
    val lastDifficulty: Int,
    val nextReviewDate: Long,
    val lastReviewDate: Long,
    val reviewLocations: List<String> = emptyList()
)

@Serializable
data class ApiUserStats(
    val userId: String,
    val correctAnswers: Int,
    val totalAnswers: Int,
    val streakDays: Int,
    val maxStreakDays: Int,
    val lastStudyDate: Long,
    val totalStudyDays: Int
)

@Serializable
data class ApiLocation(
    val id: String,
    val userId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val creationDate: Long
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)