package com.example.flashcardapp.data.repository

import android.app.Application
import android.util.Log
import com.example.flashcardapp.api.ApiRepository
import com.example.flashcardapp.data.database.FlashcardAppDatabase
import com.example.flashcardapp.data.entities.*
import com.example.flashcardapp.services.LocationService
import com.example.flashcardapp.utils.SpacedRepetitionAlgorithm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.max
import kotlin.random.Random

/**
 * Repositório principal que gerencia todas as operações de dados
 */
class FlashcardRepository(
    private val application: Application
) {
    private val database = FlashcardAppDatabase.getDatabase(application)
    private val userStatsDao = database.userStatsDao()
    private val deckDao = database.deckDao()
    private val flashcardDao = database.flashcardDao()
    private val studyInfoDao = database.studyInfoDao()
    private val locationDao = database.locationDao()

    private val spacedRepetition = SpacedRepetitionAlgorithm()
    private val locationService = LocationService(application)

    // Criar o ApiRepository passando this como parâmetro para evitar recursão
    private val apiRepository by lazy { ApiRepository(application, this) }

    // UserStats operations
    val userStats: Flow<UserStats> = userStatsDao.getUserStats()

    suspend fun initializeUserStats() {
        userStatsDao.initializeIfNeeded()
    }

    suspend fun updateAnswerStats(isCorrect: Boolean) {
        userStatsDao.updateAnswerStats(if (isCorrect) 1 else 0)
    }

    suspend fun updateStreakStats() {
        val stats = userStatsDao.getUserStatsSync() ?: UserStats()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val yesterday = today - 24 * 60 * 60 * 1000
        val lastStudyDay = Calendar.getInstance().apply {
            timeInMillis = stats.lastStudyDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val streakDays = when {
            lastStudyDay == today -> stats.streakDays // Já estudou hoje
            lastStudyDay == yesterday -> stats.streakDays + 1 // Estudou ontem, continuando streak
            else -> 1 // Streak resetado
        }

        val totalDays = if (lastStudyDay != today) stats.totalStudyDays + 1 else stats.totalStudyDays
        val maxStreak = max(streakDays, stats.maxStreakDays)

        userStatsDao.updateStreakStats(
            streakDays = streakDays,
            maxStreak = maxStreak,
            studyDate = today,
            totalDays = totalDays
        )
    }

    // Deck operations
    fun getAllDecks() = deckDao.getAllDecks()

    fun getCardCountForDeck(deckId: Long) = deckDao.getCardCountForDeck(deckId)

    fun getDueCardCountForDeck(deckId: Long) = deckDao.getDueCardCountForDeck(deckId)

    suspend fun createDeck(name: String, description: String?): Long {
        return apiRepository.createDeck(name, description)
    }

    suspend fun updateDeck(deck: Deck) {
        deckDao.updateDeck(deck)
        apiRepository.updateDeck(deck)
    }

    suspend fun deleteDeck(deck: Deck) {
        deckDao.deleteDeck(deck)
        apiRepository.deleteDeck(deck)
    }

    // Flashcard operations
    fun getFlashcardsByDeck(deckId: Long) = flashcardDao.getFlashcardsByDeck(deckId)

    suspend fun getDueFlashcardsForDeck(deckId: Long): List<Flashcard> {
        val dueCards = flashcardDao.getDueFlashcardsForDeck(deckId)

        // Sincroniza com a API antes de retornar
        apiRepository.syncFlashcards(deckId)

        return dueCards
    }

    suspend fun getAllDueFlashcards(): List<Flashcard> {
        return flashcardDao.getAllDueFlashcards()
    }

    suspend fun createBasicFlashcard(deckId: Long, question: String, answer: String): Long {
        return apiRepository.createBasicFlashcard(deckId, question, answer)
    }

    suspend fun createQuizFlashcard(deckId: Long, question: String, answer: String, options: List<String>): Long {
        return apiRepository.createQuizFlashcard(deckId, question, answer, options)
    }

    suspend fun createClozeFlashcard(deckId: Long, fullText: String, hiddenText: String): Long {
        return apiRepository.createClozeFlashcard(deckId, fullText, hiddenText)
    }

    suspend fun createInputFlashcard(deckId: Long, question: String, answer: String): Long {
        return apiRepository.createInputFlashcard(deckId, question, answer)
    }

    suspend fun updateFlashcard(flashcard: Flashcard) {
        flashcardDao.updateFlashcard(flashcard)
        apiRepository.updateFlashcard(flashcard)
    }

    suspend fun deleteFlashcard(flashcard: Flashcard) {
        flashcardDao.deleteFlashcard(flashcard)
        apiRepository.deleteFlashcard(flashcard)
    }

    // Study and review operations
    suspend fun getStudyInfo(flashcardId: Long) = studyInfoDao.getStudyInfo(flashcardId)

    suspend fun updateStudyInfo(studyInfo: StudyInfo) {
        studyInfoDao.insertOrUpdateStudyInfo(studyInfo)
    }

    suspend fun reviewFlashcard(flashcardId: Long, rating: Int) {
        val studyInfo = studyInfoDao.getStudyInfo(flashcardId) ?: return

        // Tenta obter localização atual
        var currentLocationId: Long? = null

        locationService.getCurrentLocation { location ->
            if (location != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val nearestLocation = locationDao.getNearestLocation(
                        location.latitude,
                        location.longitude
                    )

                    if (nearestLocation != null) {
                        currentLocationId = nearestLocation.locationId
                        // Atualiza o studyInfo com a nova localização
                        studyInfoDao.addReviewLocation(flashcardId, nearestLocation.locationId)
                    }
                }
            }
        }

        // Atualiza as informações de estudo
        val updatedStudyInfo = spacedRepetition.updateStudyInfo(studyInfo, rating, currentLocationId)
        studyInfoDao.insertOrUpdateStudyInfo(updatedStudyInfo)

        // Atualiza estatísticas do usuário
        updateAnswerStats(rating > 0)
        updateStreakStats()
    }

    // Location operations
    fun getAllLocations() = locationDao.getAllLocations()

    suspend fun getLocationCount() = locationDao.getLocationCount()

    suspend fun addLocation(name: String, latitude: Double, longitude: Double): Long? {
        val count = locationDao.getLocationCount()
        return if (count < 7) {
            val locationId = locationDao.insertLocation(
                StudyLocation(
                    name = name,
                    latitude = latitude,
                    longitude = longitude
                )
            )

            locationId
        } else {
            null // Limite de 7 localizações atingido
        }
    }

    suspend fun updateLocation(location: StudyLocation) {
        locationDao.updateLocation(location)
        apiRepository.updateLocation(location)
    }

    suspend fun deleteLocation(location: StudyLocation) {
        locationDao.deleteLocation(location)
        apiRepository.deleteLocation(location)
    }

    suspend fun getNearestLocation(latitude: Double, longitude: Double) =
        locationDao.getNearestLocation(latitude, longitude)

    suspend fun getFlashcardsForNewLocation(locationId: Long, limit: Int = 10): List<Flashcard> {
        // Busca todos os flashcards disponíveis
        val allFlashcards = flashcardDao.getAllDueFlashcards()

        // Busca informações de estudo para todos os flashcards
        val flashcardIds = allFlashcards.map { it.flashcardId }
        val studyInfos = studyInfoDao.getStudyInfoBatch(flashcardIds)

        // Mapa para acesso rápido de studyInfo por flashcardId
        val studyInfoMap = studyInfos.associateBy { it.flashcardId }

        // Filtra e ordena flashcards com base na localização
        val flashcardsWithPriority = allFlashcards.mapNotNull { flashcard ->
            val studyInfo = studyInfoMap[flashcard.flashcardId] ?: return@mapNotNull null

            // Calcula probabilidade baseada na localização
            val probability = spacedRepetition.calculateLocationBasedProbability(studyInfo, locationId)

            // Adiciona um elemento aleatório para não ser sempre o mesmo padrão
            val randomFactor = Random.nextDouble(0.0, 0.2)
            val finalPriority = probability + randomFactor

            Pair(flashcard, finalPriority)
        }

        // Ordena por prioridade (maior primeiro) e retorna os primeiros 'limit' flashcards
        return flashcardsWithPriority
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    // Sincronização inicial
    suspend fun syncAllData() {
        // Chama os métodos de sincronização no ApiRepository
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiRepository.syncDecks()
                apiRepository.syncLocations()
                apiRepository.syncUserStats()
            } catch (e: Exception) {
                Log.e("FlashcardRepository", "Error syncing data", e)
            }
        }
    }
}