package com.example.flashcardapp.utils

import com.example.flashcardapp.data.entities.StudyInfo
import kotlin.math.max
import kotlin.math.min


/**
 * Algoritmo de repetição espaçada
 */
class SpacedRepetitionAlgorithm {
    // Constantes para o algoritmo
    private val MIN_EASE_FACTOR = 1.3
    private val MAX_EASE_FACTOR = 2.5
    private val MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000L

    /**
     * Atualiza as informações de estudo com base na avaliação do usuário
     * @param studyInfo Informações de estudo atuais
     * @param rating Avaliação do usuário (0-4)
     * @param locationId ID da localização atual (opcional)
     * @return StudyInfo atualizado
     */
    fun updateStudyInfo(studyInfo: StudyInfo, rating: Int, locationId: Long? = null): StudyInfo {
        val now = System.currentTimeMillis()
        studyInfo.lastReviewDate = now
        studyInfo.lastDifficulty = rating

        when (rating) {
            0 -> { // Errei
                studyInfo.repetitions = 0
                studyInfo.interval = 0
                studyInfo.easeFactor = MIN_EASE_FACTOR
                studyInfo.nextReviewDate = now + (6 * 60 * 60 * 1000) // 6 horas depois
            }
            1 -> { // Bom
                studyInfo.easeFactor = max(MIN_EASE_FACTOR, studyInfo.easeFactor - 0.05)
                updateInterval(studyInfo, 1.0)
            }
            2 -> { // Fácil
                studyInfo.easeFactor = min(MAX_EASE_FACTOR, studyInfo.easeFactor + 0.05)
                updateInterval(studyInfo, 1.3)
            }
        }

        if (rating > 0) {
            studyInfo.repetitions++
        }

        if (locationId != null && !studyInfo.reviewLocations.split("|").contains(locationId.toString())) {
            studyInfo.reviewLocations = if (studyInfo.reviewLocations.isEmpty()) {
                locationId.toString()
            } else {
                "${studyInfo.reviewLocations}|$locationId"
            }
        }

        return studyInfo
    }

    private fun updateInterval(studyInfo: StudyInfo, multiplier: Double) {
        val interval = when (studyInfo.repetitions) {
            0 -> 1
            else -> (max(1.0, studyInfo.interval.toDouble()) * studyInfo.easeFactor * multiplier).toInt()
        }

        studyInfo.interval = max(1, interval)
        studyInfo.nextReviewDate = studyInfo.lastReviewDate + (studyInfo.interval * MILLISECONDS_PER_DAY)
    }
}