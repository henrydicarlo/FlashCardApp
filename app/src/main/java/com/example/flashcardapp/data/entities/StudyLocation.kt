package com.example.flashcardapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade para localizações favoritas
 */
@Entity(tableName = "locations")
data class StudyLocation(
    @PrimaryKey(autoGenerate = true) val locationId: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val creationDate: Long = System.currentTimeMillis()
)