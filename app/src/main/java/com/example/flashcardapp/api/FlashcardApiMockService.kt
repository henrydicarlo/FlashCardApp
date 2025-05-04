package com.example.flashcardapp.api

import com.example.flashcardapp.api.model.*
import kotlinx.coroutines.delay
import java.util.*

/**
 * Serviço mock que simula uma API para uso durante desenvolvimento
 */
class FlashcardApiMockService {
    private val decks = mutableMapOf<String, ApiDeck>()
    private val flashcards = mutableMapOf<String, ApiFlashcard>()
    private val studyInfos = mutableMapOf<String, ApiStudyInfo>()
    private val locations = mutableMapOf<String, ApiLocation>()
    private var userStats = ApiUserStats(
        userId = "user_1",
        correctAnswers = 0,
        totalAnswers = 0,
        streakDays = 0,
        maxStreakDays = 0,
        lastStudyDate = 0,
        totalStudyDays = 0
    )

    // Simula lag de rede
    private suspend fun simulateNetworkDelay() {
        delay(300) // 300ms de delay
    }

    // Decks
    suspend fun getDecks(userId: String): List<ApiDeck> {
        simulateNetworkDelay()
        return decks.values.filter { it.userId == userId }.toList()
    }

    suspend fun getDeck(deckId: String): ApiDeck? {
        simulateNetworkDelay()
        return decks[deckId]
    }

    suspend fun createDeck(deck: ApiDeck): ApiDeck {
        simulateNetworkDelay()
        val newId = UUID.randomUUID().toString()
        val newDeck = deck.copy(id = newId)
        decks[newId] = newDeck
        return newDeck
    }

    suspend fun updateDeck(deck: ApiDeck): ApiDeck? {
        simulateNetworkDelay()
        if (decks.containsKey(deck.id)) {
            decks[deck.id] = deck
            return deck
        }
        return null
    }

    suspend fun deleteDeck(deckId: String): Boolean {
        simulateNetworkDelay()
        if (decks.containsKey(deckId)) {
            decks.remove(deckId)
            // Remove todos os flashcards associados
            flashcards.entries.removeIf { it.value.deckId == deckId }
            return true
        }
        return false
    }

    // Flashcards
    suspend fun getFlashcards(deckId: String): List<ApiFlashcard> {
        simulateNetworkDelay()
        return flashcards.values.filter { it.deckId == deckId }.toList()
    }

    suspend fun createFlashcard(flashcard: ApiFlashcard): ApiFlashcard {
        simulateNetworkDelay()
        val newId = UUID.randomUUID().toString()
        val newFlashcard = flashcard.copy(id = newId)
        flashcards[newId] = newFlashcard

        // Cria automaticamente um StudyInfo para este flashcard
        val studyInfo = ApiStudyInfo(
            flashcardId = newId,
            easeFactor = 2.5,
            interval = 0,
            repetitions = 0,
            lastDifficulty = 0,
            nextReviewDate = 0,
            lastReviewDate = 0
        )
        studyInfos[newId] = studyInfo

        return newFlashcard
    }

    suspend fun updateFlashcard(flashcard: ApiFlashcard): ApiFlashcard? {
        simulateNetworkDelay()
        if (flashcards.containsKey(flashcard.id)) {
            flashcards[flashcard.id] = flashcard
            return flashcard
        }
        return null
    }

    suspend fun deleteFlashcard(flashcardId: String): Boolean {
        simulateNetworkDelay()
        if (flashcards.containsKey(flashcardId)) {
            flashcards.remove(flashcardId)
            studyInfos.remove(flashcardId)
            return true
        }
        return false
    }

    // StudyInfo
    suspend fun getStudyInfo(flashcardId: String): ApiStudyInfo? {
        simulateNetworkDelay()
        return studyInfos[flashcardId]
    }

    suspend fun updateStudyInfo(studyInfo: ApiStudyInfo): ApiStudyInfo {
        simulateNetworkDelay()
        studyInfos[studyInfo.flashcardId] = studyInfo
        return studyInfo
    }

    // UserStats
    suspend fun getUserStats(userId: String): ApiUserStats {
        simulateNetworkDelay()
        return userStats
    }

    suspend fun updateUserStats(stats: ApiUserStats): ApiUserStats {
        simulateNetworkDelay()
        userStats = stats
        return stats
    }

    // Locations
    suspend fun getLocations(userId: String): List<ApiLocation> {
        simulateNetworkDelay()
        return locations.values.filter { it.userId == userId }.toList()
    }

    suspend fun createLocation(location: ApiLocation): ApiLocation {
        simulateNetworkDelay()
        val newId = UUID.randomUUID().toString()
        val newLocation = location.copy(id = newId)
        locations[newId] = newLocation
        return newLocation
    }

    suspend fun updateLocation(location: ApiLocation): ApiLocation? {
        simulateNetworkDelay()
        if (locations.containsKey(location.id)) {
            locations[location.id] = location
            return location
        }
        return null
    }

    suspend fun deleteLocation(locationId: String): Boolean {
        simulateNetworkDelay()
        if (locations.containsKey(locationId)) {
            locations.remove(locationId)
            return true
        }
        return false
    }

    // Flashcards for location
    suspend fun getFlashcardsForLocation(locationId: String, limit: Int = 10): List<ApiFlashcard> {
        simulateNetworkDelay()

        // Obtém todos os flashcards que não foram estudados nesta localização
        val flashcardsNotStudiedHere = flashcards.values.filter { flashcard ->
            val studyInfo = studyInfos[flashcard.id]
            studyInfo?.reviewLocations?.contains(locationId) != true
        }

        return flashcardsNotStudiedHere.take(limit)
    }
}