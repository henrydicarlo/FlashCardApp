package com.example.flashcardapp.data.entities

import androidx.room.TypeConverter
import com.example.flashcardapp.data.entities.FlashcardType

class Converters {

    @TypeConverter
    fun fromFlashcardType(value: FlashcardType): String {
        return value.name
    }

    @TypeConverter
    fun toFlashcardType(value: String): FlashcardType {
        return FlashcardType.valueOf(value)
    }
}