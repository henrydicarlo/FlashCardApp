package com.example.flashcardapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Entidade para armazenar estatísticas do usuário
 */
@Serializable
@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1, // Singleton - apenas uma instância
    var correctAnswers: Int = 0,
    var totalAnswers: Int = 0,
    var streakDays: Int = 0,
    var maxStreakDays: Int = 0,
    var lastStudyDate: Long = 0, // Timestamp do último estudo
    var totalStudyDays: Int = 0
)