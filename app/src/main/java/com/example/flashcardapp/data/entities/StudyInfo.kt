package com.example.flashcardapp.data.entities

import androidx.room.*

/**
 * Entidade para informações de estudo de flashcard
 */
@Entity(tableName = "study_info",
    foreignKeys = [
        ForeignKey(
            entity = Flashcard::class,
            parentColumns = ["flashcardId"],
            childColumns = ["flashcard_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("flashcard_id")]
)
data class StudyInfo(
    @PrimaryKey @ColumnInfo(name = "flashcard_id") val flashcardId: Long,
    var easeFactor: Double = 2.5, // Fator de facilidade inicial
    var interval: Int = 0, // Intervalo em dias
    var repetitions: Int = 0, // Número de repetições
    var lastDifficulty: Int = 0, // Última avaliação (0-4)
    var nextReviewDate: Long = 0, // Data da próxima revisão
    var lastReviewDate: Long = 0, // Data da última revisão
    // Armazena IDs de localizações onde este cartão foi revisado
    // Formato: "1|3|5" para localizações 1, 3 e 5
    var reviewLocations: String = ""
)