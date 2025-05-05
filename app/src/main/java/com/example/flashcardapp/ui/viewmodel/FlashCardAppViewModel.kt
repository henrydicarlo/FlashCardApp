package com.example.flashcardapp.ui.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.data.database.FlashcardAppDatabase
import androidx.room.Room.databaseBuilder
import com.example.flashcardapp.data.entities.Deck
import com.example.flashcardapp.data.entities.Flashcard
import com.example.flashcardapp.data.entities.StudyLocation
import com.example.flashcardapp.data.repository.FlashcardRepository
import com.example.flashcardapp.services.LocationService
import com.example.flashcardapp.services.SyncService
import com.example.flashcardapp.ui.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel principal do aplicativo
 */
class FlashcardAppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FlashcardRepository(application)
    private val locationService = LocationService(application)

    // Estado para estatísticas do usuário
    private val _userStatsUiState = MutableStateFlow(UserStatsUiState())
    val userStatsUiState: StateFlow<UserStatsUiState> = _userStatsUiState.asStateFlow()

    // Estado para listagem de baralhos
    private val _deckListUiState = MutableStateFlow(DeckListUiState())
    val deckListUiState: StateFlow<DeckListUiState> = _deckListUiState.asStateFlow()

    // Estado para sessão de estudo
    private val _studySessionUiState = MutableStateFlow(StudySessionUiState())
    val studySessionUiState: StateFlow<StudySessionUiState> = _studySessionUiState.asStateFlow()

    // Estado para localizações
    private val _locationsUiState = MutableStateFlow(LocationsUiState())
    val locationsUiState: StateFlow<LocationsUiState> = _locationsUiState.asStateFlow()

    // Flashcards para estudar na sessão atual
    private var studySessionCards = mutableListOf<Flashcard>()

    init {
        viewModelScope.launch {
            // Inicializa estatísticas do usuário se necessário
            repository.initializeUserStats()

            // Carrega estatísticas do usuário
            repository.userStats.collect { stats ->
                _userStatsUiState.value = UserStatsUiState(
                    correctAnswerRate = if (stats.totalAnswers > 0)
                        stats.correctAnswers.toFloat() / stats.totalAnswers
                    else 0f,
                    streakDays = stats.streakDays,
                    maxStreakDays = stats.maxStreakDays,
                    totalStudyDays = stats.totalStudyDays,
                    isLoading = false
                )
            }
        }

        // Carrega lista de baralhos com informações de contagem
        viewModelScope.launch {
            val decksFlow = repository.getAllDecks()
            decksFlow.collect { deckList ->
                val decksWithStats = mutableListOf<DeckWithStats>()

                for (deck in deckList) {
                    val cardCount = repository.getCardCountForDeck(deck.deckId).first()
                    val dueCount = repository.getDueCardCountForDeck(deck.deckId).first()

                    decksWithStats.add(
                        DeckWithStats(
                            deck = deck,
                            cardCount = cardCount,
                            dueCardCount = dueCount
                        )
                    )
                }

                _deckListUiState.value = DeckListUiState(
                    decks = decksWithStats,
                    isLoading = false
                )
            }
        }

        // Carrega localizações
        viewModelScope.launch {
            repository.getAllLocations().collect { locationList ->
                _locationsUiState.value = LocationsUiState(
                    locations = locationList,
                    canAddMore = locationList.size < 7,
                    isLoading = false
                )
            }
        }
    }

    // Funções para gerenciamento de baralhos

    fun createDeck(name: String, description: String? = null) {
        viewModelScope.launch {
            repository.createDeck(name, description)
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            repository.deleteDeck(deck)
        }
    }

    // Funções para gerenciamento de flashcards

    fun createBasicFlashcard(deckId: Long, question: String, answer: String) {
        viewModelScope.launch {
            repository.createBasicFlashcard(deckId, question, answer)
        }
    }

    fun createQuizFlashcard(deckId: Long, question: String, answer: String, options: List<String>) {
        viewModelScope.launch {
            repository.createQuizFlashcard(deckId, question, answer, options)
        }
    }

    fun createClozeFlashcard(deckId: Long, fullText: String, hiddenText: String) {
        viewModelScope.launch {
            repository.createClozeFlashcard(deckId, fullText, hiddenText)
        }
    }

    fun createInputFlashcard(deckId: Long, question: String, answer: String) {
        viewModelScope.launch {
            repository.createInputFlashcard(deckId, question, answer)
        }
    }


    // Adicionado
    suspend fun getAllFlashcardsForDeck(deckId: Long): List<Flashcard> {
        return repository.getFlashcardsByDeck(deckId).first()
    }

    fun deleteFlashcard(flashcard: Flashcard) {
        viewModelScope.launch {
            repository.deleteFlashcard(flashcard)
        }
    }


    // Funções para sessão de estudo

    fun startStudySession(deckId: Long? = null) {
        viewModelScope.launch {
            _studySessionUiState.value = _studySessionUiState.value.copy(isLoading = true)

            studySessionCards = if (deckId != null) {
                repository.getDueFlashcardsForDeck(deckId).toMutableList()
            } else {
                repository.getAllDueFlashcards().toMutableList()
            }

            if (studySessionCards.isNotEmpty()) {
                _studySessionUiState.value = StudySessionUiState(
                    currentFlashcard = studySessionCards.first(),
                    remainingCards = studySessionCards.size,
                    isLoading = false
                )
            } else {
                _studySessionUiState.value = StudySessionUiState(
                    isLoading = false,
                    isCompleted = true
                )
            }
        }
    }

    fun revealAnswer() {
        _studySessionUiState.value = _studySessionUiState.value.copy(
            isAnswerRevealed = true
        )
    }

    fun rateCard(rating: Int) {
        val currentCard = _studySessionUiState.value.currentFlashcard ?: return

        viewModelScope.launch {
            repository.reviewFlashcard(currentCard.flashcardId, rating)

            // Remove o cartão atual e avança
            if (studySessionCards.isNotEmpty()) {
                studySessionCards.removeAt(0)
            }

            if (studySessionCards.isNotEmpty()) {
                _studySessionUiState.value = StudySessionUiState(
                    currentFlashcard = studySessionCards.first(),
                    remainingCards = studySessionCards.size,
                    isLoading = false
                )
            } else {
                _studySessionUiState.value = StudySessionUiState(
                    isLoading = false,
                    isCompleted = true
                )
            }
        }
    }

    // Funções para localizações

    fun addStudyLocation(name: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.addLocation(name, latitude, longitude)
        }
    }

    fun deleteLocation(location: StudyLocation) {
        viewModelScope.launch {
            repository.deleteLocation(location)
        }
    }

    fun updateCurrentLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val nearest = repository.getNearestLocation(latitude, longitude)
            _locationsUiState.value = _locationsUiState.value.copy(
                currentLocation = nearest
            )
            _studySessionUiState.value = _studySessionUiState.value.copy(
                currentLocation = nearest
            )
        }
    }

    fun startLocationBasedStudy(locationId: Long) {
        viewModelScope.launch {
            _studySessionUiState.value = _studySessionUiState.value.copy(isLoading = true)

            studySessionCards = repository.getFlashcardsForNewLocation(locationId).toMutableList()

            if (studySessionCards.isNotEmpty()) {
                _studySessionUiState.value = StudySessionUiState(
                    currentFlashcard = studySessionCards.first(),
                    remainingCards = studySessionCards.size,
                    isLoading = false
                )
            } else {
                _studySessionUiState.value = StudySessionUiState(
                    isLoading = false,
                    isCompleted = true
                )
            }
        }
    }

    private val syncService = SyncService(
        context = application,
        database = FlashcardAppDatabase.getDatabase(application)
    )

    /*private val syncService = SyncService(
        context = application,
        database = databaseBuilder(
            application,
            FlashcardAppDatabase::class.java,
            "flashcard_app_database"
        ).build()
    )*/


    fun sync() {
        viewModelScope.launch {
            syncService.syncData()
        }
    }
}

