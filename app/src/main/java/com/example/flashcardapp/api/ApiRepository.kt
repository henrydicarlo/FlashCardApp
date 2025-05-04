package com.example.flashcardapp.api

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.flashcardapp.api.model.*
import com.example.flashcardapp.data.database.FlashcardAppDatabase
import com.example.flashcardapp.data.entities.*
import com.example.flashcardapp.data.repository.FlashcardRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.*

/**
 * Repositório que integra a API com o banco de dados local
 */
class ApiRepository(
    private val application: Application,
    private val externalRepository: FlashcardRepository? = null
) {
    // Acesso direto ao banco de dados
    private val database = FlashcardAppDatabase.getDatabase(application)

    // Acesso direto aos DAOs
    private val userStatsDao = database.userStatsDao()
    private val deckDao = database.deckDao()
    private val flashcardDao = database.flashcardDao()
    private val studyInfoDao = database.studyInfoDao()
    private val locationDao = database.locationDao()

    private val apiMockService = FlashcardApiMockService()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Verifica se há conexão com a internet
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    // Converters para traduzir entre modelos da API e locais

    // Deck
    private fun convertApiDeckToLocal(apiDeck: ApiDeck): Deck {
        return Deck(
            deckId = apiDeck.id.toLongOrNull() ?: 0,
            name = apiDeck.name,
            description = apiDeck.description,
            creationDate = apiDeck.creationDate
        )
    }

    private fun convertLocalDeckToApi(deck: Deck): ApiDeck {
        return ApiDeck(
            id = deck.deckId.toString(),
            name = deck.name,
            description = deck.description,
            creationDate = deck.creationDate,
            userId = "user_1", // Usuário fixo para o mock
            isPublic = false
        )
    }

    // Flashcard
    private fun convertApiFlashcardToLocal(apiFlashcard: ApiFlashcard): Flashcard {
        return Flashcard(
            flashcardId = apiFlashcard.id.toLongOrNull() ?: 0,
            deckId = apiFlashcard.deckId.toLongOrNull() ?: 0,
            type = FlashcardType.valueOf(apiFlashcard.type),
            question = apiFlashcard.question,
            answer = apiFlashcard.answer,
            options = apiFlashcard.options,
            fullText = apiFlashcard.fullText,
            creationDate = apiFlashcard.creationDate
        )
    }

    private fun convertLocalFlashcardToApi(flashcard: Flashcard): ApiFlashcard {
        return ApiFlashcard(
            id = flashcard.flashcardId.toString(),
            deckId = flashcard.deckId.toString(),
            type = flashcard.type.name,
            question = flashcard.question,
            answer = flashcard.answer,
            options = flashcard.options,
            fullText = flashcard.fullText,
            creationDate = flashcard.creationDate
        )
    }

    // StudyInfo
    private fun convertApiStudyInfoToLocal(apiStudyInfo: ApiStudyInfo): StudyInfo {
        return StudyInfo(
            flashcardId = apiStudyInfo.flashcardId.toLongOrNull() ?: 0,
            easeFactor = apiStudyInfo.easeFactor,
            interval = apiStudyInfo.interval,
            repetitions = apiStudyInfo.repetitions,
            lastDifficulty = apiStudyInfo.lastDifficulty,
            nextReviewDate = apiStudyInfo.nextReviewDate,
            lastReviewDate = apiStudyInfo.lastReviewDate,
            reviewLocations = apiStudyInfo.reviewLocations.joinToString("|")
        )
    }

    private fun convertLocalStudyInfoToApi(studyInfo: StudyInfo): ApiStudyInfo {
        return ApiStudyInfo(
            flashcardId = studyInfo.flashcardId.toString(),
            easeFactor = studyInfo.easeFactor,
            interval = studyInfo.interval,
            repetitions = studyInfo.repetitions,
            lastDifficulty = studyInfo.lastDifficulty,
            nextReviewDate = studyInfo.nextReviewDate,
            lastReviewDate = studyInfo.lastReviewDate,
            reviewLocations = if (studyInfo.reviewLocations.isBlank())
                emptyList()
            else studyInfo.reviewLocations.split("|")
        )
    }

    // Location
    private fun convertApiLocationToLocal(apiLocation: ApiLocation): StudyLocation {
        return StudyLocation(
            locationId = apiLocation.id.toLongOrNull() ?: 0,
            name = apiLocation.name,
            latitude = apiLocation.latitude,
            longitude = apiLocation.longitude,
            creationDate = apiLocation.creationDate
        )
    }

    private fun convertLocalLocationToApi(location: StudyLocation): ApiLocation {
        return ApiLocation(
            id = location.locationId.toString(),
            userId = "user_1", // Usuário fixo para o mock
            name = location.name,
            latitude = location.latitude,
            longitude = location.longitude,
            creationDate = location.creationDate
        )
    }

    // Operações com Decks
    suspend fun syncDecks() {
        if (!isNetworkAvailable()) return

        try {
            // Busca decks locais
            val localDecks = deckDao.getAllDecks().first()

            // Busca decks da API
            val apiDecks = apiMockService.getDecks("user_1")

            // Para cada deck da API, verifica se existe localmente
            apiDecks.forEach { apiDeck ->
                val localDeck = localDecks.find { it.deckId.toString() == apiDeck.id }
                if (localDeck == null) {
                    // Se não existe localmente, cria
                    deckDao.insertDeck(
                        Deck(
                            name = apiDeck.name,
                            description = apiDeck.description
                        )
                    )
                }
            }

            // Para cada deck local, verifica se existe na API
            localDecks.forEach { localDeck ->
                val apiDeck = apiDecks.find { it.id == localDeck.deckId.toString() }
                if (apiDeck == null) {
                    // Se não existe na API, cria
                    apiMockService.createDeck(convertLocalDeckToApi(localDeck))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun createDeck(name: String, description: String?): Long {
        val deckId = deckDao.insertDeck(Deck(name = name, description = description))

        if (isNetworkAvailable()) {
            try {
                val deck = Deck(deckId = deckId, name = name, description = description)
                apiMockService.createDeck(convertLocalDeckToApi(deck))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return deckId
    }

    suspend fun updateDeck(deck: Deck) {
        deckDao.updateDeck(deck)

        if (isNetworkAvailable()) {
            try {
                apiMockService.updateDeck(convertLocalDeckToApi(deck))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteDeck(deck: Deck) {
        deckDao.deleteDeck(deck)

        if (isNetworkAvailable()) {
            try {
                apiMockService.deleteDeck(deck.deckId.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Operações com Flashcards
    suspend fun syncFlashcards(deckId: Long) {
        if (!isNetworkAvailable()) return

        try {
            // Busca flashcards locais
            val localFlashcards = flashcardDao.getFlashcardsByDeck(deckId).first()

            // Busca flashcards da API
            val apiFlashcards = apiMockService.getFlashcards(deckId.toString())

            // Para cada flashcard da API, verifica se existe localmente
            apiFlashcards.forEach { apiFlashcard ->
                val localFlashcard = localFlashcards.find { it.flashcardId.toString() == apiFlashcard.id }
                if (localFlashcard == null) {
                    // Se não existe localmente, cria
                    val newFlashcard = convertApiFlashcardToLocal(apiFlashcard)
                    when (newFlashcard.type) {
                        FlashcardType.BASIC -> createBasicFlashcard(newFlashcard.deckId, newFlashcard.question, newFlashcard.answer)
                        FlashcardType.QUIZ -> {
                            val options = newFlashcard.options?.split("|") ?: listOf()
                            createQuizFlashcard(newFlashcard.deckId, newFlashcard.question, newFlashcard.answer, options)
                        }
                        FlashcardType.CLOZE -> createClozeFlashcard(newFlashcard.deckId, newFlashcard.fullText ?: "", newFlashcard.answer)
                        FlashcardType.INPUT -> createInputFlashcard(newFlashcard.deckId, newFlashcard.question, newFlashcard.answer)
                    }
                }
            }

            // Para cada flashcard local, verifica se existe na API
            localFlashcards.forEach { localFlashcard ->
                val apiFlashcard = apiFlashcards.find { it.id == localFlashcard.flashcardId.toString() }
                if (apiFlashcard == null) {
                    // Se não existe na API, cria
                    apiMockService.createFlashcard(convertLocalFlashcardToApi(localFlashcard))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun createBasicFlashcard(deckId: Long, question: String, answer: String): Long {
        val flashcardId = flashcardDao.insertFlashcard(
            Flashcard(
                deckId = deckId,
                type = FlashcardType.BASIC,
                question = question,
                answer = answer
            )
        )

        // Inicializa informações de estudo
        studyInfoDao.insertOrUpdateStudyInfo(StudyInfo(flashcardId = flashcardId))

        if (isNetworkAvailable()) {
            try {
                val flashcard = Flashcard(
                    flashcardId = flashcardId,
                    deckId = deckId,
                    type = FlashcardType.BASIC,
                    question = question,
                    answer = answer
                )
                apiMockService.createFlashcard(convertLocalFlashcardToApi(flashcard))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return flashcardId
    }

    suspend fun createQuizFlashcard(deckId: Long, question: String, answer: String, options: List<String>): Long {
        val optionsString = options.joinToString("|")
        val flashcardId = flashcardDao.insertFlashcard(
            Flashcard(
                deckId = deckId,
                type = FlashcardType.QUIZ,
                question = question,
                answer = answer,
                options = optionsString
            )
        )

        studyInfoDao.insertOrUpdateStudyInfo(StudyInfo(flashcardId = flashcardId))

        if (isNetworkAvailable()) {
            try {
                val flashcard = Flashcard(
                    flashcardId = flashcardId,
                    deckId = deckId,
                    type = FlashcardType.QUIZ,
                    question = question,
                    answer = answer,
                    options = optionsString
                )
                apiMockService.createFlashcard(convertLocalFlashcardToApi(flashcard))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return flashcardId
    }

    suspend fun createClozeFlashcard(deckId: Long, fullText: String, hiddenText: String): Long {
        val flashcardId = flashcardDao.insertFlashcard(
            Flashcard(
                deckId = deckId,
                type = FlashcardType.CLOZE,
                question = fullText.replace(hiddenText, "..."),
                answer = hiddenText,
                fullText = fullText
            )
        )

        studyInfoDao.insertOrUpdateStudyInfo(StudyInfo(flashcardId = flashcardId))

        if (isNetworkAvailable()) {
            try {
                val flashcard = Flashcard(
                    flashcardId = flashcardId,
                    deckId = deckId,
                    type = FlashcardType.CLOZE,
                    question = fullText.replace(hiddenText, "..."),
                    answer = hiddenText,
                    fullText = fullText
                )
                apiMockService.createFlashcard(convertLocalFlashcardToApi(flashcard))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return flashcardId
    }

    suspend fun createInputFlashcard(deckId: Long, question: String, answer: String): Long {
        val flashcardId = flashcardDao.insertFlashcard(
            Flashcard(
                deckId = deckId,
                type = FlashcardType.INPUT,
                question = question,
                answer = answer
            )
        )

        studyInfoDao.insertOrUpdateStudyInfo(StudyInfo(flashcardId = flashcardId))

        if (isNetworkAvailable()) {
            try {
                val flashcard = Flashcard(
                    flashcardId = flashcardId,
                    deckId = deckId,
                    type = FlashcardType.INPUT,
                    question = question,
                    answer = answer
                )
                apiMockService.createFlashcard(convertLocalFlashcardToApi(flashcard))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return flashcardId
    }

    suspend fun updateFlashcard(flashcard: Flashcard) {
        flashcardDao.updateFlashcard(flashcard)

        if (isNetworkAvailable()) {
            try {
                apiMockService.updateFlashcard(convertLocalFlashcardToApi(flashcard))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteFlashcard(flashcard: Flashcard) {
        flashcardDao.deleteFlashcard(flashcard)

        if (isNetworkAvailable()) {
            try {
                apiMockService.deleteFlashcard(flashcard.flashcardId.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Operações com StudyInfo
    suspend fun syncStudyInfo(flashcardId: Long) {
        if (!isNetworkAvailable()) return

        try {
            // Busca studyInfo local
            val localStudyInfo = studyInfoDao.getStudyInfo(flashcardId)

            // Busca studyInfo da API
            val apiStudyInfo = apiMockService.getStudyInfo(flashcardId.toString())

            if (localStudyInfo != null && apiStudyInfo != null) {
                // Verifica qual é o mais recente
                if (localStudyInfo.lastReviewDate > apiStudyInfo.lastReviewDate) {
                    // Local é mais recente, atualiza na API
                    apiMockService.updateStudyInfo(convertLocalStudyInfoToApi(localStudyInfo))
                } else if (localStudyInfo.lastReviewDate < apiStudyInfo.lastReviewDate) {
                    // API é mais recente, atualiza localmente
                    studyInfoDao.insertOrUpdateStudyInfo(convertApiStudyInfoToLocal(apiStudyInfo))
                }
            } else if (localStudyInfo != null) {
                // Existe apenas localmente, cria na API
                apiMockService.updateStudyInfo(convertLocalStudyInfoToApi(localStudyInfo))
            } else if (apiStudyInfo != null) {
                // Existe apenas na API, cria localmente
                studyInfoDao.insertOrUpdateStudyInfo(convertApiStudyInfoToLocal(apiStudyInfo))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun reviewFlashcard(flashcardId: Long, rating: Int) {
        // Aqui vamos usar o externalRepository se disponível
        externalRepository?.reviewFlashcard(flashcardId, rating)

        if (isNetworkAvailable()) {
            try {
                // Busca o studyInfo atualizado
                val localStudyInfo = studyInfoDao.getStudyInfo(flashcardId)
                if (localStudyInfo != null) {
                    // Atualiza na API
                    apiMockService.updateStudyInfo(convertLocalStudyInfoToApi(localStudyInfo))
                }

                // Também atualiza as estatísticas do usuário
                val userStats = userStatsDao.getUserStatsSync() ?: UserStats()
                val apiUserStats = ApiUserStats(
                    userId = "user_1",
                    correctAnswers = userStats.correctAnswers,
                    totalAnswers = userStats.totalAnswers,
                    streakDays = userStats.streakDays,
                    maxStreakDays = userStats.maxStreakDays,
                    lastStudyDate = userStats.lastStudyDate,
                    totalStudyDays = userStats.totalStudyDays
                )
                apiMockService.updateUserStats(apiUserStats)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Operações com Locations
    suspend fun syncLocations() {
        if (!isNetworkAvailable()) return

        try {
            // Busca localizações locais
            val localLocations = locationDao.getAllLocations().first()

            // Busca localizações da API
            val apiLocations = apiMockService.getLocations("user_1")

            // Para cada localização da API, verifica se existe localmente
            apiLocations.forEach { apiLocation ->
                val localLocation = localLocations.find { it.locationId.toString() == apiLocation.id }
                if (localLocation == null) {
                    // Se não existe localmente, cria
                    locationDao.insertLocation(
                        StudyLocation(
                            name = apiLocation.name,
                            latitude = apiLocation.latitude,
                            longitude = apiLocation.longitude
                        )
                    )
                }
            }

            // Para cada localização local, verifica se existe na API
            localLocations.forEach { localLocation ->
                val apiLocation = apiLocations.find { it.id == localLocation.locationId.toString() }
                if (apiLocation == null) {
                    // Se não existe na API, cria
                    apiMockService.createLocation(convertLocalLocationToApi(localLocation))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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

            // Sincroniza com a API
            syncLocations()

            locationId
        } else {
            null // Limite de 7 localizações atingido
        }
    }

    suspend fun updateLocation(location: StudyLocation) {
        locationDao.updateLocation(location)

        if (isNetworkAvailable()) {
            try {
                apiMockService.updateLocation(convertLocalLocationToApi(location))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteLocation(location: StudyLocation) {
        locationDao.deleteLocation(location)

        if (isNetworkAvailable()) {
            try {
                apiMockService.deleteLocation(location.locationId.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Operações com UserStats
    suspend fun syncUserStats() {
        if (!isNetworkAvailable()) return

        try {
            // Busca estatísticas locais
            val localStats = userStatsDao.getUserStatsSync() ?: UserStats()

            // Busca estatísticas da API
            val apiStats = apiMockService.getUserStats("user_1")

            // Verifica qual é o mais recente
            if (localStats.lastStudyDate > apiStats.lastStudyDate) {
                // Local é mais recente, atualiza na API
                val apiUserStats = ApiUserStats(
                    userId = "user_1",
                    correctAnswers = localStats.correctAnswers,
                    totalAnswers = localStats.totalAnswers,
                    streakDays = localStats.streakDays,
                    maxStreakDays = localStats.maxStreakDays,
                    lastStudyDate = localStats.lastStudyDate,
                    totalStudyDays = localStats.totalStudyDays
                )
                apiMockService.updateUserStats(apiUserStats)
            } else if (localStats.lastStudyDate < apiStats.lastStudyDate) {
                // API é mais recente, seria ideal atualizar localmente
                // Como não temos um método direto para isso, vamos usar uma abordagem alternativa
                coroutineScope.launch {
                    // Poderia ser implementado de forma mais robusta em uma versão futura
                    // Por enquanto, apenas logamos a inconsistência
                    Log.d("ApiRepository", "API stats are more recent but can't update local directly")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Inicialização
    init {
        // Inicia sincronização de dados quando o repositório é criado
        coroutineScope.launch {
            syncDecks()
            syncLocations()
            syncUserStats()
        }
    }
}