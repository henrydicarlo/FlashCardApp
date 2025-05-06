package com.example.flashcardapp.services

import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.util.Log
import com.example.flashcardapp.data.database.FlashcardAppDatabase
import com.example.flashcardapp.ui.model.SyncData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout

class SyncService(
    private val context: Context,
    private val database: FlashcardAppDatabase
) {
    private val TAG = "SyncService"
    private val client = HttpClient (CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000 // 15 segundos
        }
    }

    /**
     * Sincroniza dados entre o aplicativo e o servidor
     *
     * @return true se a sincronização foi bem-sucedida, false caso contrário
     */
    suspend fun syncData(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Coleta os dados locais
            val syncData = SyncData(
                decks = database.deckDao().getAll(),
                flashcards = database.flashcardDao().getAll(),
                studyInfos = database.studyInfoDao().getAll(),
                locations = database.locationDao().getAll(),
                userStats = database.userStatsDao().getAll()
            )
            Log.i("Teste","Flashcard -> size " +syncData.flashcards.size)
            syncData.flashcards.forEach { t ->
                Log.i("Teste","Dec " + t.question)
            }

            Log.i("Teste","Vou conectar e enviar os dados ")
            // Envia dados para o servidor
            val servidor = "https://flashcardserver.onrender.com/api/sync"
            val servidorT = "http://192.168.15.6:8080/api/sync"
            // abra o cmd e digite o ipconfig e pegue o ip local ipv4
            val response = client.post(servidorT) {
                contentType(ContentType.Application.Json)
                setBody(syncData)
            }

            if (response.status != HttpStatusCode.OK) {
                Log.e(TAG, "Erro ao enviar dados: ${response.status}")
                return@withContext false
            }

            // Baixa dados do servidor
            return@withContext downloadAndSaveServerData()

        } catch (e: Exception) {
            // Handle error (log or notify user)
            Log.e(TAG, "Erro durante sincronização", e)
            return@withContext false
        } finally {
            client.close()
        }
    }

    /**
     * Baixa apenas os dados do servidor e salva no banco de dados local
     * Esta função pode ser chamada independentemente para apenas baixar dados
     *
     * @return true se o download foi bem-sucedido, false caso contrário
     */
    suspend fun downloadAndSaveServerData(): Boolean = withContext(Dispatchers.IO) {
        val client = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                })
            }
        }

        try {
            // Busca dados do servidor
            Log.d(TAG, "Baixando dados do servidor...")
            val servidor = "https://flashcardserver.onrender.com/api/sync"
            val servidorT = "http://192.168.15.6:8080/api/sync"
            val serverData = client.get(servidorT).body<SyncData>()

            Log.d(TAG, "Dados recebidos: ${serverData.decks.size} decks, " +
                    "${serverData.flashcards.size} flashcards, " +
                    "${serverData.studyInfos.size} estudos, " +
                    "${serverData.locations.size} localizações, " +
                    "${serverData.userStats.size} estatísticas")

            // Atualiza o banco do aplicativo
            database.runInTransaction {
                try {
                    // Clear existing data
                    //database.deckDao().deleteAll()
                    //database.flashcardDao().deleteAll()
                    //database.studyInfoDao().deleteAll()
                    //database.locationDao().deleteAll()
                    //database.userStatsDao().deleteAll()

                    // Insere todos os dados recebidos do servidor
                    //database.deckDao().insertAll(serverData.decks)
                    //database.flashcardDao().insertAll(serverData.flashcards)
                    //database.studyInfoDao().insertAll(serverData.studyInfos)
                    //database.locationDao().insertAll(serverData.locations)
                    //database.userStatsDao().insertAll(serverData.userStats)

                    Log.d(TAG, "Banco de dados local atualizado com sucesso")
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao salvar dados no banco local", e)
                    throw e // Relanço a exceção para reverter a transação
                }
            }

            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao baixar ou salvar dados", e)
            return@withContext false
        } finally {
            client.close()
        }
    }
}
