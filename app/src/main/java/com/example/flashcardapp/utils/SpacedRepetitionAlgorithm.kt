package com.example.flashcardapp.utils

import com.example.flashcardapp.data.entities.StudyInfo
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Algoritmo de repetição espaçada inspirado no SM-2 (usado pelo Anki)
 */
class SpacedRepetitionAlgorithm {
    // Constantes para o algoritmo
    private val MIN_EASE_FACTOR = 1.3
    private val MAX_EASE_FACTOR = 2.5
    private val MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000L

    /**
     * Atualiza as informações de estudo com base na avaliação do usuário
     * @param studyInfo Informações de estudo atuais
     * @param rating Avaliação do usuário (0-3)
     *        0 = Difícil (erro)
     *        1 = Bom (acertou com alguma dificuldade)
     *        2 = Fácil (acertou sem esforço)
     * @param locationId ID da localização atual (opcional)
     * @return StudyInfo atualizado
     */
    fun updateStudyInfo(studyInfo: StudyInfo, rating: Int, locationId: Long? = null): StudyInfo {
        val now = System.currentTimeMillis()
        studyInfo.lastReviewDate = now
        studyInfo.lastDifficulty = rating

        // Calcula intervalo com base no algoritmo Supermemo SM-2
        when (rating) {
            0 -> { // Difícil/Erro
                studyInfo.repetitions = 0
                studyInfo.interval = 0
                studyInfo.easeFactor = max(MIN_EASE_FACTOR, studyInfo.easeFactor - 0.2)

                // Revê em 30 minutos (erro grave)
                studyInfo.nextReviewDate = now + (30 * 60 * 1000)
            }
            1 -> { // Bom/Alguma dificuldade
                if (studyInfo.repetitions == 0) {
                    // Primeira revisão bem-sucedida
                    studyInfo.interval = 1
                    studyInfo.repetitions = 1
                } else if (studyInfo.repetitions == 1) {
                    // Segunda revisão bem-sucedida
                    studyInfo.interval = 3
                    studyInfo.repetitions = 2
                } else {
                    // Revisões subsequentes
                    studyInfo.interval = (studyInfo.interval * studyInfo.easeFactor).roundToInt()
                    studyInfo.repetitions++
                }

                studyInfo.easeFactor = max(MIN_EASE_FACTOR, studyInfo.easeFactor - 0.15)
                studyInfo.nextReviewDate = now + (studyInfo.interval * MILLISECONDS_PER_DAY)
            }
            2 -> { // Fácil/Sem esforço
                if (studyInfo.repetitions == 0) {
                    // Primeira revisão bem-sucedida
                    studyInfo.interval = 2
                    studyInfo.repetitions = 1
                } else if (studyInfo.repetitions == 1) {
                    // Segunda revisão bem-sucedida
                    studyInfo.interval = 5
                    studyInfo.repetitions = 2
                } else {
                    // Revisões subsequentes
                    studyInfo.interval = (studyInfo.interval * studyInfo.easeFactor * 1.2).roundToInt()
                    studyInfo.repetitions++
                }

                studyInfo.easeFactor = min(MAX_EASE_FACTOR, studyInfo.easeFactor + 0.1)
                studyInfo.nextReviewDate = now + (studyInfo.interval * MILLISECONDS_PER_DAY)
            }
        }

        // Se o intervalo for muito longo, limitar a um máximo razoável (6 meses)
        val maxInterval = 180 // dias
        if (studyInfo.interval > maxInterval) {
            studyInfo.interval = maxInterval
            studyInfo.nextReviewDate = now + (maxInterval * MILLISECONDS_PER_DAY)
        }

        // Registra a localização onde o flashcard foi estudado
        if (locationId != null) {
            val locations = if (studyInfo.reviewLocations.isEmpty()) {
                mutableListOf()
            } else {
                studyInfo.reviewLocations.split("|").toMutableList()
            }

            val locationIdStr = locationId.toString()
            if (!locations.contains(locationIdStr)) {
                locations.add(locationIdStr)
                studyInfo.reviewLocations = locations.joinToString("|")
            }
        }

        return studyInfo
    }

    /**
     * Calcula a probabilidade de mostrar um flashcard com base na localização
     * Flashcards estudados na mesma localização têm menor probabilidade
     *
     * @param studyInfo Informações de estudo do flashcard
     * @param locationId ID da localização atual
     * @return Valor entre 0.0 e 1.0, onde 1.0 significa alta probabilidade
     */
    fun calculateLocationBasedProbability(studyInfo: StudyInfo, locationId: Long): Double {
        // Se não foi estudado em nenhuma localização, alta probabilidade
        if (studyInfo.reviewLocations.isEmpty()) {
            return 1.0
        }

        val locations = studyInfo.reviewLocations.split("|")
        val locationIdStr = locationId.toString()

        // Se nunca foi estudado nesta localização, alta probabilidade
        if (!locations.contains(locationIdStr)) {
            return 1.0
        }

        // Calcula quantas vezes foi estudado na mesma localização
        // comparado com o total de localizações
        val sameLocationCount = locations.count { it == locationIdStr }
        val totalLocations = locations.size

        // Quanto mais vezes estudado neste local, menor a probabilidade
        val baseProbability = 0.3 + (0.7 * (1.0 - (sameLocationCount.toDouble() / totalLocations)))

        // Limitar a probabilidade mínima para 0.1 (10%)
        return max(0.1, baseProbability)
    }
}