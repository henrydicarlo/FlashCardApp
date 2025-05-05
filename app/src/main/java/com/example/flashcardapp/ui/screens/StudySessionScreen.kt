package com.example.flashcardapp.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

    LaunchedEffect(key1 = deckId, key2 = locationBased) {
        if (locationBased && locationsState.currentLocation != null) {
            viewModel.startLocationBasedStudy(locationsState.currentLocation!!.locationId)
        } else {
            viewModel.startStudySession(if (deckId > 0) deckId else null)
        }
    }

    var canNavigateBack by remember { mutableStateOf(true) }

    BackHandler(enabled = !canNavigateBack) {
        Toast.makeText(context, "Você ainda não avaliou. Diga o que achou dessa pergunta.", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (locationBased && locationsState.currentLocation != null)
                            "Estudo: ${locationsState.currentLocation!!.name}"
                        else
                            "Sessão de Estudo"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (canNavigateBack) {
                            navController.popBackStack()
                        } else {
                            Toast.makeText(
                                context,
                                "Você ainda não avaliou. Diga o que achou dessa pergunta.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }) {
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
                studySessionState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                studySessionState.isCompleted -> StudySessionCompleted(onBackClick = { navController.popBackStack() })
                studySessionState.currentFlashcard != null -> FlashcardStudyContent(
                    flashcard = studySessionState.currentFlashcard!!,
                    isAnswerRevealed = studySessionState.isAnswerRevealed,
                    remainingCards = studySessionState.remainingCards,
                    onRevealAnswer = { viewModel.revealAnswer() },
                    onRateCard = {
                        viewModel.rateCard(it)
                        canNavigateBack = true
                    },
                    setCanNavigateBack = { canNavigateBack = it }
                )
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
    onRateCard: (Int) -> Unit,
    setCanNavigateBack: (Boolean) -> Unit
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var userInput by remember { mutableStateOf("") }
    var hasCheckedInput by remember { mutableStateOf(false) }

    val options = remember(flashcard.options) {
        flashcard.options?.split("|")?.shuffled() ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { 1f - (remainingCards.toFloat() / (remainingCards + 1)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

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
                when (flashcard.type) {
                    FlashcardType.CLOZE -> {
                        val full = flashcard.fullText ?: ""
                        val hidden = flashcard.answer
                        val masked = full.replace(hidden, "...")

                        Text(
                            text = if (isAnswerRevealed) full else masked,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp),
                            color = Color.Black
                        )

                        if (!isAnswerRevealed) {
                            Button(
                                onClick = { onRevealAnswer() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Revelar resposta")
                            }
                        }
                    }

                    FlashcardType.QUIZ -> {
                        Text(
                            text = flashcard.question,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        options.forEach { option ->
                            val isCorrect = option == flashcard.answer
                            val isSelected = option == selectedOption

                            val bgColor = when {
                                selectedOption != null && isCorrect -> Color(0xFF74F197)
                                selectedOption != null && isSelected && !isCorrect -> Color(
                                    0xC9EF3346
                                )
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Button(
                                onClick = {
                                    if (selectedOption == null) {
                                        selectedOption = option
                                        onRevealAnswer()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = bgColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(option, color = Color.Black)
                            }
                        }
                    }

                    FlashcardType.INPUT -> {
                        Text(
                            text = flashcard.question,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        if (!isAnswerRevealed) {
                            OutlinedTextField(
                                value = userInput,
                                onValueChange = { userInput = it },
                                label = { Text("Sua resposta") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    hasCheckedInput = true
                                    onRevealAnswer()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = userInput.isNotBlank()
                            ) {
                                Text("Verificar")
                            }
                        } else {
                            val isCorrect = userInput.trim().equals(flashcard.answer.trim(), ignoreCase = true)
                            val feedbackColor = if (isCorrect) Color(0xFF06EC46) else Color(
                                0xFFF6152C
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (isCorrect) "✅ Você acertou!" else "❌ Você errou!",
                                color = feedbackColor,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Resposta correta: ${flashcard.answer}",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    FlashcardType.BASIC -> {
                        Text(
                            text = flashcard.question,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        if (!isAnswerRevealed) {
                            Button(
                                onClick = { onRevealAnswer() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Revelar resposta")
                            }
                        } else {
                            Divider(modifier = Modifier.padding(vertical = 16.dp))
                            Text("Resposta:", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = flashcard.answer,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isAnswerRevealed && (flashcard.type != FlashcardType.INPUT || hasCheckedInput)) {
            setCanNavigateBack(false)

            Text(
                text = "O que achou dessa questão?",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onRateCard(0) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) { Text("Difícil", color = Color.Black) }

                Button(
                    onClick = { onRateCard(1) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) { Text("Bom", color = Color.Black) }

                Button(
                    onClick = { onRateCard(2) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) { Text("Fácil", color = Color.Black) }
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
        Text("Sessão Concluída!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Parabéns! Você completou todos os cartões disponíveis para revisão.",
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
