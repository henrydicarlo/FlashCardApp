package com.example.flashcardapp.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flashcardapp.data.entities.Flashcard
import com.example.flashcardapp.data.entities.FlashcardType
import com.example.flashcardapp.ui.viewmodel.FlashcardAppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySessionScreen(
    deckId: Long,
    locationBased: Boolean = false,
    navController: NavController,
    viewModel: FlashcardAppViewModel
) {
    val studySessionState by viewModel.studySessionUiState.collectAsState()
    val locationsState by viewModel.locationsUiState.collectAsState()

    // Iniciar a sessão de estudo quando a tela é carregada
    LaunchedEffect(key1 = deckId, key2 = locationBased) {
        if (locationBased && locationsState.currentLocation != null) {
            viewModel.startLocationBasedStudy(locationsState.currentLocation!!.locationId)
        } else {
            viewModel.startStudySession(if (deckId > 0) deckId else null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (locationBased && locationsState.currentLocation != null) {
                        Text("Estudo: ${locationsState.currentLocation!!.name}")
                    } else {
                        Text("Sessão de Estudo")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (locationBased && locationsState.currentLocation != null) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Baseado em localização",
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                studySessionState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                studySessionState.isCompleted -> {
                    StudySessionCompleted(onBackClick = { navController.popBackStack() })
                }

                studySessionState.currentFlashcard != null -> {
                    FlashcardStudyContent(
                        flashcard = studySessionState.currentFlashcard!!,
                        isAnswerRevealed = studySessionState.isAnswerRevealed,
                        remainingCards = studySessionState.remainingCards,
                        onRevealAnswer = { viewModel.revealAnswer() },
                        onRateCard = { rating -> viewModel.rateCard(rating) }
                    )
                }
            }
        }
    }
}

@Composable
fun FlashcardStudyContent(
    flashcard: Flashcard,
    isAnswerRevealed: Boolean,
    remainingCards: Int,
    onRevealAnswer: () -> Unit,
    onRateCard: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progresso
        LinearProgressIndicator(
            progress = { 1f - (remainingCards.toFloat() / (remainingCards + 1)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Cartão
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Pergunta
                Text(
                    text = flashcard.question,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Resposta (se estiver revelada)
                if (isAnswerRevealed) {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    Text(
                        text = "Resposta:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    when (flashcard.type) {
                        FlashcardType.QUIZ -> {
                            Text(
                                text = flashcard.answer,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                        FlashcardType.CLOZE -> {
                            Text(
                                text = flashcard.fullText ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                        else -> {
                            Text(
                                text = flashcard.answer,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botões de ação
        if (!isAnswerRevealed) {
            Button(
                onClick = onRevealAnswer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Mostrar Resposta")
            }
        } else {
            Text(
                text = "Como você se saiu?",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onRateCard(0) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Difícil")
                }

                Button(
                    onClick = { onRateCard(1) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Bom")
                }

                Button(
                    onClick = { onRateCard(2) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Text("Fácil")
                }
            }
        }
    }
}

@Composable
fun StudySessionCompleted(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sessão Concluída!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Parabéns! Você completou todos os cartões disponíveis para revisão.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Voltar")
        }
    }
}