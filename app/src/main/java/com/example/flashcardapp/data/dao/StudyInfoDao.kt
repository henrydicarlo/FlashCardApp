package com.example.flashcardapp.data.dao

import androidx.room.*
import com.example.flashcardapp.data.entities.StudyInfo

/**
 * DAO para informações de estudo
 */
@Dao
interface StudyInfoDao {
    @Query("SELECT * FROM study_info WHERE flashcard_id = :flashcardId")
    suspend fun getStudyInfo(flashcardId: Long): StudyInfo?

    @Query("SELECT * FROM study_info WHERE flashcard_id IN (:flashcardIds)")
    suspend fun getStudyInfoBatch(flashcardIds: List<Long>): List<StudyInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStudyInfo(studyInfo: StudyInfo)

    @Query("UPDATE study_info SET reviewLocations = reviewLocations || CASE WHEN reviewLocations = '' THEN '' ELSE '|' END || :locationId WHERE flashcard_id = :flashcardId")
    suspend fun addReviewLocation(flashcardId: Long, locationId: Long)

    @Query("SELECT COUNT(*) FROM study_info WHERE reviewLocations LIKE '%' || :locationId || '%'")
    suspend fun getReviewCountAtLocation(locationId: Long): Int
}