package com.example.flashcardapp.data.repository

import android.app.Application
import android.location.Location
import com.example.flashcardapp.data.database.FlashcardAppDatabase
import com.example.flashcardapp.data.entities.Deck
import com.example.flashcardapp.data.entities.Flashcard
import com.example.flashcardapp.data.entities.FlashcardType
import com.example.flashcardapp.data.entities.StudyInfo
import com.example.flashcardapp.data.entities.StudyLocation
import com.example.flashcardapp.data.entities.UserStats
import com.example.flashcardapp.services.LocationService
import com.example.flashcardapp.utils.SpacedRepetitionAlgorithm
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.max


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

    // Operações Estatísticas
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

    // Operações do Deck
    fun getAllDecks() = deckDao.getAllDecks()

    fun getCardCountForDeck(deckId: Long) = deckDao.getCardCountForDeck(deckId)

    fun getDueCardCountForDeck(deckId: Long) = deckDao.getDueCardCountForDeck(deckId)

    suspend fun createDeck(name: String, description: String?) =
        deckDao.insertDeck(Deck(name = name, description = description))

    suspend fun updateDeck(deck: Deck) = deckDao.updateDeck(deck)

    suspend fun deleteDeck(deck: Deck) = deckDao.deleteDeck(deck)

    // Operações dos Flashcards
    fun getFlashcardsByDeck(deckId: Long) = flashcardDao.getFlashcardsByDeck(deckId)

    suspend fun getDueFlashcardsForDeck(deckId: Long, now: Long, currentLocationId: Long?): List<Flashcard> {
        val allDue = flashcardDao.getDueFlashcardsForDeck(deckId, now)

        return if (currentLocationId == null) {
            allDue
        } else {
            allDue.sortedBy {
                if (it.createdLocationId == currentLocationId) 1 else 0
            }
        }
    }

    suspend fun getCurrentLocationSync(): Location? = withContext(Dispatchers.IO) {
        val deferred = CompletableDeferred<Location?>()
        locationService.getCurrentLocation { location ->
            deferred.complete(location)
        }
        deferred.await()
    }
    suspend fun getAllDueFlashcards(now: Long, currentLocationId: Long?): List<Flashcard> {
        val allDueCards = flashcardDao.getAllDueFlashcards(now)

        return if (currentLocationId == null) {
            allDueCards
        } else {
            // Separa os cards por localização
            val (sameLocation, otherLocations) = allDueCards.partition {
                it.createdLocationId == currentLocationId
            }

            // Diminui a chance dos da mesma localização (ex: 25% da lista)
            val reducedSameLocation = sameLocation.shuffled().take((sameLocation.size * 0.25).toInt())

            // Junta tudo e embaralha para não criar padrão
            (otherLocations + reducedSameLocation).shuffled()
        }
    }

    suspend fun createBasicFlashcard(deckId: Long, question: String, answer: String): Long {
        var createdLocationId: Long? = null

        // Tenta obter a localização atual
        val locationDeferred = kotlinx.coroutines.CompletableDeferred<Location?>()
        locationService.getCurrentLocation { location ->
            locationDeferred.complete(location)
        }

        val location = locationDeferred.await()
        if (location != null) {
            val nearestLocation = locationDao.getNearestLocation(location.latitude, location.longitude)
            createdLocationId = nearestLocation?.locationId
        }

        val flashcardId = flashcardDao.insertFlashcard(
            Flashcard(
                deckId = deckId,
                type = FlashcardType.BASIC,
                question = question,
                answer = answer,
                createdLocationId = createdLocationId
            )
        )

        studyInfoDao.insertOrUpdateStudyInfo(StudyInfo(flashcardId = flashcardId))
        return flashcardId
    }


    suspend fun createQuizFlashcard(deckId: Long, question: String, answer: String, options: List<String>): Long {
        val optionsString = options.joinToString("|")

        val locationDeferred = kotlinx.coroutines.CompletableDeferred<Location?>()
        locationService.getCurrentLocation { location ->
            locationDeferred.complete(location)
        }
        val location = locationDeferred.await()
        val nearestLocation = location?.let {
            locationDao.getNearestLocation(it.latitude, it.longitude)
        }

        val flashcardId = flashcardDao.insertFlashcard(
            Flashcard(
                deckId = deckId,
                type = FlashcardType.QUIZ,
                question = question,
                answer = answer,
                options = optionsString,
                createdLocationId = nearestLocation?.locationId
            )
        )

        studyInfoDao.insertOrUpdateStudyInfo(StudyInfo(flashcardId = flashcardId))
        return flashcardId
    }


    suspend fun createClozeFlashcard(deckId: Long, fullText: String, hiddenText: String): Long {
        val locationDeferred = kotlinx.coroutines.CompletableDeferred<Location?>()
        locationService.getCurrentLocation { location ->
            locationDeferred.complete(location)
        }
        val location = locationDeferred.await()
        val nearestLocation = location?.let {
            locationDao.getNearestLocation(it.latitude, it.longitude)
        }

        val flashcardId = flashcardDao.insertFlashcard(
            Flashcard(
                deckId = deckId,
                type = FlashcardType.CLOZE,
                question = fullText.replace(hiddenText, "..."),
                answer = hiddenText,
                fullText = fullText,
                createdLocationId = nearestLocation?.locationId
            )
        )

        studyInfoDao.insertOrUpdateStudyInfo(StudyInfo(flashcardId = flashcardId))
        return flashcardId
    }


    suspend fun createInputFlashcard(deckId: Long, question: String, answer: String): Long {
        val locationDeferred = kotlinx.coroutines.CompletableDeferred<Location?>()
        locationService.getCurrentLocation { location ->
            locationDeferred.complete(location)
        }
        val location = locationDeferred.await()
        val nearestLocation = location?.let {
            locationDao.getNearestLocation(it.latitude, it.longitude)
        }

        val flashcardId = flashcardDao.insertFlashcard(
            Flashcard(
                deckId = deckId,
                type = FlashcardType.INPUT,
                question = question,
                answer = answer,
                createdLocationId = nearestLocation?.locationId
            )
        )

        studyInfoDao.insertOrUpdateStudyInfo(StudyInfo(flashcardId = flashcardId))
        return flashcardId
    }


    suspend fun updateFlashcard(flashcard: Flashcard) = flashcardDao.updateFlashcard(flashcard)

    suspend fun deleteFlashcard(flashcard: Flashcard) = flashcardDao.deleteFlashcard(flashcard)

    // Operações de Estudo e Revisão
    suspend fun getStudyInfo(flashcardId: Long) = studyInfoDao.getStudyInfo(flashcardId)

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

    // Operações de Local
    fun getAllLocations() = locationDao.getAllLocations()

    suspend fun getLocationCount() = locationDao.getLocationCount()

    suspend fun addLocation(name: String, latitude: Double, longitude: Double): Long? {
        val count = locationDao.getLocationCount()
        return if (count < 7) {
            locationDao.insertLocation(
                StudyLocation(
                    name = name,
                    latitude = latitude,
                    longitude = longitude
                )
            )
        } else {
            null // Limite de 7 localizações atingido
        }
    }

    suspend fun updateLocation(location: StudyLocation) = locationDao.updateLocation(location)

    suspend fun deleteLocation(location: StudyLocation) = locationDao.deleteLocation(location)

    suspend fun getNearestLocation(latitude: Double, longitude: Double) =
        locationDao.getNearestLocation(latitude, longitude)

    suspend fun getFlashcardsForNewLocation(locationId: Long, limit: Int = 10) =
        flashcardDao.getFlashcardsNotReviewedAtLocation(locationId, limit)


}