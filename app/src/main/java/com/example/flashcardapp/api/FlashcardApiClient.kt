package com.example.flashcardapp.api

import android.util.Log
import com.example.flashcardapp.api.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class FlashcardApiClient {
    private val BASE_URL = "https://flashcard-api.example.com/api"
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("FlashcardApiClient", message)
                }
            }
            level = LogLevel.ALL
        }
    }

    // User ID simulado - em produção, viria de autenticação
    private val userId = "user_${System.currentTimeMillis()}"

    // Endpoints para Decks
    suspend fun getDecks(): List<ApiDeck> {
        val response = client.get("$BASE_URL/decks") {
            parameter("userId", userId)
        }
        return response.body()
    }

    suspend fun getDeck(deckId: String): ApiDeck {
        val response = client.get("$BASE_URL/decks/$deckId")
        return response.body()
    }

    suspend fun createDeck(name: String, description: String?): ApiDeck {
        val response = client.post("$BASE_URL/decks") {
            contentType(ContentType.Application.Json)
            setBody(ApiDeck(
                id = "",
                name = name,
                description = description,
                creationDate = System.currentTimeMillis(),
                userId = userId
            ))
        }
        return response.body()
    }

    suspend fun updateDeck(deck: ApiDeck): ApiDeck {
        val response = client.put("$BASE_URL/decks/${deck.id}") {
            contentType(ContentType.Application.Json)
            setBody(deck)
        }
        return response.body()
    }

    suspend fun deleteDeck(deckId: String): Boolean {
        val response = client.delete("$BASE_URL/decks/$deckId")
        return response.status.isSuccess()
    }

    // Endpoints para Flashcards
    suspend fun getFlashcards(deckId: String): List<ApiFlashcard> {
        val response = client.get("$BASE_URL/decks/$deckId/flashcards")
        return response.body()
    }

    suspend fun createFlashcard(flashcard: ApiFlashcard): ApiFlashcard {
        val response = client.post("$BASE_URL/flashcards") {
            contentType(ContentType.Application.Json)
            setBody(flashcard)
        }
        return response.body()
    }

    suspend fun updateFlashcard(flashcard: ApiFlashcard): ApiFlashcard {
        val response = client.put("$BASE_URL/flashcards/${flashcard.id}") {
            contentType(ContentType.Application.Json)
            setBody(flashcard)
        }
        return response.body()
    }

    suspend fun deleteFlashcard(flashcardId: String): Boolean {
        val response = client.delete("$BASE_URL/flashcards/$flashcardId")
        return response.status.isSuccess()
    }

    // Endpoints para StudyInfo
    suspend fun getStudyInfo(flashcardId: String): ApiStudyInfo {
        val response = client.get("$BASE_URL/studyinfo/$flashcardId")
        return response.body()
    }

    suspend fun updateStudyInfo(studyInfo: ApiStudyInfo): ApiStudyInfo {
        val response = client.put("$BASE_URL/studyinfo/${studyInfo.flashcardId}") {
            contentType(ContentType.Application.Json)
            setBody(studyInfo)
        }
        return response.body()
    }

    // Endpoints para UserStats
    suspend fun getUserStats(): ApiUserStats {
        val response = client.get("$BASE_URL/stats") {
            parameter("userId", userId)
        }
        return response.body()
    }

    suspend fun updateUserStats(stats: ApiUserStats): ApiUserStats {
        val response = client.put("$BASE_URL/stats") {
            contentType(ContentType.Application.Json)
            setBody(stats)
        }
        return response.body()
    }

    // Endpoints para Locations
    suspend fun getLocations(): List<ApiLocation> {
        val response = client.get("$BASE_URL/locations") {
            parameter("userId", userId)
        }
        return response.body()
    }

    suspend fun createLocation(location: ApiLocation): ApiLocation {
        val response = client.post("$BASE_URL/locations") {
            contentType(ContentType.Application.Json)
            setBody(location)
        }
        return response.body()
    }

    suspend fun updateLocation(location: ApiLocation): ApiLocation {
        val response = client.put("$BASE_URL/locations/${location.id}") {
            contentType(ContentType.Application.Json)
            setBody(location)
        }
        return response.body()
    }

    suspend fun deleteLocation(locationId: String): Boolean {
        val response = client.delete("$BASE_URL/locations/$locationId")
        return response.status.isSuccess()
    }

    // Endpoints para Shared Decks
    suspend fun getPublicDecks(): List<ApiDeck> {
        val response = client.get("$BASE_URL/public/decks")
        return response.body()
    }

    suspend fun setDeckPublic(deckId: String, isPublic: Boolean): ApiDeck {
        val response = client.put("$BASE_URL/decks/$deckId/visibility") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("isPublic" to isPublic))
        }
        return response.body()
    }

    // Endpoint para obter flashcards para nova localização
    suspend fun getFlashcardsForLocation(locationId: String, limit: Int = 10): List<ApiFlashcard> {
        val response = client.get("$BASE_URL/locations/$locationId/flashcards") {
            parameter("limit", limit)
        }
        return response.body()
    }
}