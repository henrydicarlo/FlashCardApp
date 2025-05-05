package com.example.flashcardapp.services

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import com.example.flashcardapp.data.database.FlashcardAppDatabase
import com.example.flashcardapp.ui.model.SyncData
import io.ktor.client.call.body


class SyncService(
    private val context: Context,
    private val database: FlashcardAppDatabase
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun syncData() = withContext(Dispatchers.IO) {
        try {
            // Coleta os dados locais
            val syncData = SyncData(
                decks = database.deckDao().getAll(),
                flashcards = database.flashcardDao().getAll(),
                studyInfos = database.studyInfoDao().getAll(),
                locations = database.locationDao().getAll(),
                userStats = database.userStatsDao().getAll()
            )

            // Envia dados para o servidor
            client.post("http://localhost:8080/api/sync") {
                contentType(ContentType.Application.Json)
                setBody(syncData)
            }

            // Busca dados do servidor
            val serverData = client.get("http://localhost:8080/api/sync").body<SyncData>()

            // Atualiza o banco do aplicativo
            database.runInTransaction {
                // Clear existing data
                //database.deckDao().deleteAll()
                //database.flashcardDao().deleteAll()
                //database.studyInfoDao().deleteAll()
                //database.locationDao().deleteAll()
                //database.userStatsDao().deleteAll()

                // Insere todos os dados
                //database.deckDao().insertAll(serverData.decks)
                //database.flashcardDao().insertAll(serverData.flashcards)
                //database.studyInfoDao().insertAll(serverData.studyInfos)
                //database.locationDao().insertAll(serverData.locations)
                //database.userStatsDao().insertAll(serverData.userStats)
            }
        } catch (e: Exception) {
            // Handle error (log or notify user)
            e.printStackTrace()
        } finally {
            client.close()
        }
    }
}