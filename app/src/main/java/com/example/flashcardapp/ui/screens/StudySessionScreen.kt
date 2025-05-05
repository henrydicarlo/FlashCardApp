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
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flashcardapp.data.entities.Flashcard
import com.example.flashcardapp.data.entities.FlashcardType
import com.example.flashcardapp.data.entities.StudyInfo
import com.example.flashcardapp.ui.viewmodel.FlashcardAppViewModel
import com.example.flashcardapp.utils.SpacedRepetitionAlgorithm

// Definindo as cores do aplicativo
val PrimaryBlue = Color(0xFF2962FF)
val SecondaryMagenta = Color(0xFFE91E63)
val PurpleAccent = Color(0xFF7C4DFF)
val BackgroundColor = Color(0xFFF8F8F8)
val TextPrimaryColor = Color(0xFF333333)
val TextSecondaryColor = Color(0xFF757575)
val CorrectAnswerColor = Color(0xFF06EC46)
val IncorrectAnswerColor = Color(0xFFF6152C)

// Definindo o gradiente para elementos específicos
val AppGradient = Brush.linearGradient(
    colors = listOf(SecondaryMagenta, PurpleAccent, PrimaryBlue)
)

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
                            "Sessão de Estudo",
                        color = Color.White
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    if (locationBased && locationsState.currentLocation != null) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Baseado em localização",
                            modifier = Modifier.padding(end = 16.dp),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                studySessionState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryBlue
                )
                studySessionState.isCompleted -> StudySessionCompleted(onBackClick = { navController.popBackStack() })
                studySessionState.currentFlashcard != null -> FlashcardStudyContent(
                    flashcard = studySessionState.currentFlashcard!!,
                    studyInfo = studySessionState.currentStudyInfo!!,
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
    studyInfo: StudyInfo,
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
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator com o gradiente
        LinearProgressIndicator(
            progress = { 1f - (remainingCards.toFloat() / (remainingCards + 1)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            color = PrimaryBlue,
            trackColor = Color(0xFFE0E0E0)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
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
                            color = TextPrimaryColor
                        )

                        if (!isAnswerRevealed) {
                            Button(
                                onClick = { onRevealAnswer() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryBlue
                                )
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
                            modifier = Modifier.padding(bottom = 24.dp),
                            color = TextPrimaryColor
                        )

                        options.forEach { option ->
                            val isCorrect = option == flashcard.answer
                            val isSelected = option == selectedOption

                            val bgColor = when {
                                selectedOption != null && isCorrect -> CorrectAnswerColor
                                selectedOption != null && isSelected && !isCorrect -> IncorrectAnswerColor
                                else -> Color(0xFFEBEBEB)
                            }

                            val textColor = when {
                                selectedOption != null && (isCorrect || (isSelected && !isCorrect)) -> Color.White
                                else -> TextPrimaryColor
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
                                Text(option, color = textColor)
                            }
                        }
                    }

                    FlashcardType.INPUT -> {
                        Text(
                            text = flashcard.question,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp),
                            color = TextPrimaryColor
                        )

                        if (!isAnswerRevealed) {
                            OutlinedTextField(
                                value = userInput,
                                onValueChange = { userInput = it },
                                label = { Text("Sua resposta", color = TextSecondaryColor) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    hasCheckedInput = true
                                    onRevealAnswer()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = userInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryBlue,
                                    disabledContainerColor = Color(0xFFCCCCCC)
                                )
                            ) {
                                Text("Verificar")
                            }
                        } else {
                            val isCorrect =
                                userInput.trim().equals(flashcard.answer.trim(), ignoreCase = true)
                            val feedbackColor =
                                if (isCorrect) CorrectAnswerColor else IncorrectAnswerColor

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
                                textAlign = TextAlign.Center,
                                color = TextPrimaryColor
                            )
                        }
                    }

                    FlashcardType.BASIC -> {
                        Text(
                            text = flashcard.question,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp),
                            color = TextPrimaryColor
                        )

                        if (!isAnswerRevealed) {
                            Button(
                                onClick = { onRevealAnswer() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Text("Revelar resposta")
                            }
                        } else {
                            Divider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = Color(0xFFE0E0E0)
                            )
                            Text(
                                "Resposta:",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextSecondaryColor
                            )
                            Text(
                                text = flashcard.answer,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp),
                                color = TextPrimaryColor
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
                modifier = Modifier.padding(bottom = 8.dp),
                color = TextSecondaryColor
            )

            val algorithm = SpacedRepetitionAlgorithm()

            val now = System.currentTimeMillis()

            val predictions = listOf(
                0 to "Difícil",
                1 to "Bom",
                2 to "Fácil"
            ).map { (rating, label) ->
                val simulated = studyInfo.copy()
                algorithm.updateStudyInfo(simulated, rating)
                val daysUntilNextReview = ((simulated.nextReviewDate - now) / (1000 * 60 * 60 * 24)).toInt()
                val readable = when (daysUntilNextReview) {
                    0 -> "hoje"
                    1 -> "1 dia"
                    else -> "$daysUntilNextReview dias"
                }
                Triple(rating, label, readable)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                predictions.forEach { (rating, label, formattedDate) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$formattedDate",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 4.dp),
                            color = TextSecondaryColor
                        )
                        Button(
                            onClick = { onRateCard(rating) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (rating) {
                                    0 -> Color(0xFFFFF3F0) // Fundo vermelho claro para "Difícil"
                                    1 -> Color(0xFFE8F0FF) // Fundo azul claro para "Bom"
                                    else -> Color(0xFFF0FFF4) // Fundo verde claro para "Fácil"
                                }
                            )
                        ) {
                            Text(
                                label,
                                color = when (rating) {
                                    0 -> IncorrectAnswerColor.copy(alpha = 0.8f)
                                    1 -> PrimaryBlue
                                    else -> CorrectAnswerColor.copy(alpha = 0.8f)
                                }
                            )
                        }
                    }
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
            "Sessão Concluída!",
            style = MaterialTheme.typography.headlineMedium,
            color = PrimaryBlue
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Parabéns! Você completou todos os cartões disponíveis para revisão.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = TextSecondaryColor
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue
            )
        ) {
            Text("Voltar")
        }
    }
}