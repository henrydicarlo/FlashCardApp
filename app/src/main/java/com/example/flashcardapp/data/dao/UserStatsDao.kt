package com.example.flashcardapp.data.dao

import androidx.room.*
import com.example.flashcardapp.data.entities.UserStats
import kotlinx.coroutines.flow.Flow

/**
 * DAO para estatísticas do usuário
 */
@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStats>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: UserStats)

    @Query("UPDATE user_stats SET correctAnswers = correctAnswers + :correct, totalAnswers = totalAnswers + 1 WHERE id = 1")
    suspend fun updateAnswerStats(correct: Int)

    @Query("UPDATE user_stats SET streakDays = :streakDays, maxStreakDays = :maxStreak, lastStudyDate = :studyDate, totalStudyDays = :totalDays WHERE id = 1")
    suspend fun updateStreakStats(streakDays: Int, maxStreak: Int, studyDate: Long, totalDays: Int)

    @Transaction
    suspend fun initializeIfNeeded() {
        val stats = getUserStatsSync()
        if (stats == null) {
            insertOrUpdateStats(UserStats())
        }
    }

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStatsSync(): UserStats?
}