package com.example.flashcardapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Entidade para localizações favoritas
 */
@Serializable
@Entity(tableName = "locations")
data class StudyLocation(
    @PrimaryKey(autoGenerate = true) val locationId: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val creationDate: Long = System.currentTimeMillis()
)