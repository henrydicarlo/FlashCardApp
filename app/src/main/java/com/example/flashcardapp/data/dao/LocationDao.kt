package com.example.flashcardapp.data.dao

import androidx.room.*
import com.example.flashcardapp.data.entities.StudyLocation
import kotlinx.coroutines.flow.Flow

/**
 * DAO para localizações de estudo
 */
@Dao
interface LocationDao {
    @Query("SELECT * FROM locations ORDER BY name")
    fun getAllLocations(): Flow<List<StudyLocation>>

    @Query("SELECT * FROM locations WHERE locationId = :locationId")
    suspend fun getLocationById(locationId: Long): StudyLocation?

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun getLocationCount(): Int

    @Insert
    suspend fun insertLocation(location: StudyLocation): Long

    @Update
    suspend fun updateLocation(location: StudyLocation)

    @Delete
    suspend fun deleteLocation(location: StudyLocation)

    @Query("SELECT * FROM locations ORDER BY " +
            "(latitude - :lat) * (latitude - :lat) + " +
            "(longitude - :lng) * (longitude - :lng) ASC LIMIT 1")
    suspend fun getNearestLocation(lat: Double, lng: Double): StudyLocation?

    @Query("SELECT * FROM locations ORDER BY name")
    fun getAll(): List<StudyLocation>

    @Query("DELETE FROM locations")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(locations: List<StudyLocation>)

}