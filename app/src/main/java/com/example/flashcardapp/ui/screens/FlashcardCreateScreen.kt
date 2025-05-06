package com.example.flashcardapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flashcardapp.ui.viewmodel.FlashcardAppViewModel

private val BluePrimary = Color(0xFF2962FF) // Azul vibrante primário
private val BlueIcons = Color(0xFF5481FF) // Azul mais claro para ícones
private val BlueDark = Color(0xFF0039CB) // Azul escuro para elementos de destaque
private val BlueLight = Color(0xFFE3F2FD) // Azul claro para fundos secundários
private val MagentaSecondary = Color(0xFFE91E63) // Rosa/magenta para elementos complementares
private val MagentaLight = Color(0xFFFCE4EC) // Rosa claro para fundos sutis
private val PurpleTransition = Color(0xFF9C27B0) // Roxo para transições em gradientes
private val NeutralLight = Color(0xFFFAFAFA) // Neutro claro para fundos
private val NeutralDark = Color(0xFF333333) // Neutro escuro para textos principais
private val ErrorRed = Color(0xFFE53935) // Vermelho para erros
private val InfoBlue = Color(0xFF29B6F6) // Azul para informações
private val AmberAccent = Color(0xFFFFAB00) // Âmbar para elementos que precisam de destaque especial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardCreateScreen(
    deckId: Long,
    type: String,
    navController: NavController,
    viewModel: FlashcardAppViewModel
) {
    var formValid by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    when (type) {
                        "BASIC" -> Text("Criar Flashcard Básico", color = NeutralDark)
                        "QUIZ" -> Text("Criar Flashcard de Quiz", color = NeutralDark)
                        "CLOZE" -> Text("Criar Flashcard Cloze", color = NeutralDark)
                        "INPUT" -> Text("Criar Flashcard de Entrada", color = NeutralDark)
                        else -> Text("Criar Flashcard", color = NeutralDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = BlueIcons)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        when (type) {
                            "BASIC" -> BasicFlashcardForm(
                                deckId = deckId,
                                viewModel = viewModel,
                                onFormValidChange = { isValid -> formValid = isValid },
                                onSave = { navController.popBackStack() }
                            )
                            "QUIZ" -> QuizFlashcardForm(
                                deckId = deckId,
                                viewModel = viewModel,
                                onFormValidChange = { isValid -> formValid = isValid },
                                onSave = { navController.popBackStack() }
                            )
                            "CLOZE" -> ClozeFlashcardForm(
                                deckId = deckId,
                                viewModel = viewModel,
                                onFormValidChange = { isValid -> formValid = isValid },
                                onSave = { navController.popBackStack() }
                            )
                            "INPUT" -> InputFlashcardForm(
                                deckId = deckId,
                                viewModel = viewModel,
                                onFormValidChange = { isValid -> formValid = isValid },
                                onSave = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BasicFlashcardForm(
    deckId: Long,
    viewModel: FlashcardAppViewModel,
    onFormValidChange: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }

    LaunchedEffect(question, answer) {
        onFormValidChange(question.isNotBlank() && answer.isNotBlank())
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Crie um flashcard básico com uma pergunta e resposta",
            style = MaterialTheme.typography.bodyLarge,
            color = NeutralDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            label = { Text("Pergunta", color = NeutralDark.copy(alpha = 0.8f)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = NeutralDark.copy(alpha = 0.3f),
                focusedLabelColor = BluePrimary,
                unfocusedLabelColor = NeutralDark.copy(alpha = 0.7f),
                cursorColor = BluePrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = answer,
            onValueChange = { answer = it },
            label = { Text("Resposta", color = NeutralDark.copy(alpha = 0.8f)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = NeutralDark.copy(alpha = 0.3f),
                focusedLabelColor = BluePrimary,
                unfocusedLabelColor = NeutralDark.copy(alpha = 0.7f),
                cursorColor = BluePrimary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.createBasicFlashcard(deckId, question, answer)
                onSave()
            },
            enabled = question.isNotBlank() && answer.isNotBlank(),
            modifier = Modifier
                .align(Alignment.End)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MagentaSecondary,
                disabledContainerColor = MagentaSecondary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Salvar")
        }
    }
}

@Composable
fun QuizFlashcardForm(
    deckId: Long,
    viewModel: FlashcardAppViewModel,
    onFormValidChange: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    var question by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("") }
    var wrongOption1 by remember { mutableStateOf("") }
    var wrongOption2 by remember { mutableStateOf("") }
    var wrongOption3 by remember { mutableStateOf("") }

    LaunchedEffect(question, correctAnswer, wrongOption1, wrongOption2) {
        onFormValidChange(question.isNotBlank() &&
                correctAnswer.isNotBlank() &&
                wrongOption1.isNotBlank() &&
                wrongOption2.isNotBlank())
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Crie um flashcard de quiz com múltiplas opções",
            style = MaterialTheme.typography.bodyLarge,
            color = NeutralDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            label = { Text("Pergunta", color = NeutralDark.copy(alpha = 0.8f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = NeutralDark.copy(alpha = 0.3f),
                focusedLabelColor = BluePrimary,
                unfocusedLabelColor = NeutralDark.copy(alpha = 0.7f),
                cursorColor = BluePrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Resposta correta:",
            style = MaterialTheme.typography.titleSmall,
            color = BlueDark
        )

        OutlinedTextField(
            value = correctAnswer,
            onValueChange = { correctAnswer = it },
            label = { Text("Opção correta", color = NeutralDark.copy(alpha = 0.8f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = NeutralDark.copy(alpha = 0.3f),
                focusedLabelColor = BluePrimary,
                unfocusedLabelColor = NeutralDark.copy(alpha = 0.7f),
                cursorColor = BluePrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Opções incorretas:",
            style = MaterialTheme.typography.titleSmall,
            color = MagentaSecondary
        )

        OutlinedTextField(
            value = wrongOption1,
            onValueChange = { wrongOption1 = it },
            label = { Text("Opção incorreta 1", color = NeutralDark.copy(alpha = 0.8f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MagentaSecondary,
                unfocusedBorderColor = NeutralDark.copy(alpha = 0.3f),
                focusedLabelColor = MagentaSecondary,
                unfocusedLabelColor = NeutralDark.copy(alpha = 0.7f),
                cursorColor = MagentaSecondary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = wrongOption2,
            onValueChange = { wrongOption2 = it },
            label = { Text("Opção incorreta 2", color = NeutralDark.copy(alpha = 0.8f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MagentaSecondary,
                unfocusedBorderColor = NeutralDark.copy(alpha = 0.3f),
                focusedLabelColor = MagentaSecondary,
                unfocusedLabelColor = NeutralDark.copy(alpha = 0.7f),
                cursorColor = MagentaSecondary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = wrongOption3,
            onValueChange = { wrongOption3 = it },
            label = { Text("Opção incorreta 3 (opcional)", color = NeutralDark.copy(alpha = 0.8f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MagentaSecondary,
                unfocusedBorderColor = NeutralDark.copy(alpha = 0.3f),
                focusedLabelColor = MagentaSecondary,
                unfocusedLabelColor = NeutralDark.copy(alpha = 0.7f),
                cursorColor = MagentaSecondary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val options = mutableListOf(correctAnswer, wrongOption1, wrongOption2)
                if (wrongOption3.isNotBlank()) {
                    options.add(wrongOption3)
                }
                viewModel.createQuizFlashcard(deckId, question, correctAnswer, options)
                onSave()
            },
            enabled = question.isNotBlank() && correctAnswer.isNotBlank() &&
                    wrongOption1.isNotBlank() && wrongOption2.isNotBlank(),
            modifier = Modifier
                .align(Alignment.End)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MagentaSecondary,
                disabledContainerColor = MagentaSecondary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Salvar")
        }
    }
}

@Composable
fun ClozeFlashcardForm(
    deckId: Long,
    viewModel: FlashcardAppViewModel,
    onFormValidChange: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    var fullText by remember { mutableStateOf("") }
    var hiddenText by remember { mutableStateOf("") }

    LaunchedEffect(fullText, hiddenText) {
        val isValid = fullText.isNotBlank() && hiddenText.isNotBlank() &&
                fullText.contains(hiddenText)
        onFormValidChange(isValid)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Crie um flashcard cloze com texto para completar",
            style = MaterialTheme.typography.bodyLarge,
            color = NeutralDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = fullText,
            onValueChange = { fullText = it },
            label = { Text("Texto completo", color = NeutralDark.copy(alpha = 0.8f)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = NeutralDark.copy(alpha = 0.3f),
                focusedLabelColor = BluePrimary,
                unfocusedLabelColor = NeutralDark.copy(alpha = 0.7f),
                cursorColor = BluePrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = hiddenText,
            onValueChange = { hiddenText = it },
            label = { Text("Texto a esconder", color = NeutralDark.copy(alpha = 0.8f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MagentaSecondary,
                unfocusedBorderColor = NeutralDark.copy(alpha = 0.3f),
                focusedLabelColor = MagentaSecondary,
                unfocusedLabelColor = NeutralDark.copy(alpha = 0.7f),
                cursorColor = MagentaSecondary
            )
        )

        if (fullText.isNotBlank() && hiddenText.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pré-visualização:",
                style = MaterialTheme.typography.titleSmall,
                color = BlueDark
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BlueLight
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (fullText.contains(hiddenText))
                            fullText.replace(hiddenText, "...")
                        else
                            "O texto a esconder deve estar contido no texto completo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.createClozeFlashcard(deckId, fullText, hiddenText)
                onSave()
            },
            enabled = fullText.isNotBlank() && hiddenText.isNotBlank() &&
                    fullText.contains(hiddenText),
            modifier = Modifier
                .align(Alignment.End)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MagentaSecondary,
                disabledContainerColor = MagentaSecondary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Salvar")
        }
    }
}

@Composable
fun InputFlashcardForm(
    deckId: Long,
    viewModel: FlashcardAppViewModel,
    onFormValidChange: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }

    LaunchedEffect(question, answer) {
        onFormValidChange(question.isNotBlank() && answer.isNotBlank())
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Crie um flashcard que exige uma resposta específica",
            style = MaterialTheme.typography.bodyLarge,
            color = NeutralDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            label = { Text("Pergunta", color = NeutralDark.copy(alpha = 0.8f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = NeutralDark.copy(alpha = 0.3f),
                focusedLabelColor = BluePrimary,
                unfocusedLabelColor = NeutralDark.copy(alpha = 0.7f),
                cursorColor = BluePrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = answer,
            onValueChange = { answer = it },
            label = { Text("Resposta esperada", color = NeutralDark.copy(alpha = 0.8f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MagentaSecondary,
                unfocusedBorderColor = NeutralDark.copy(alpha = 0.3f),
                focusedLabelColor = MagentaSecondary,
                unfocusedLabelColor = NeutralDark.copy(alpha = 0.7f),
                cursorColor = MagentaSecondary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.createInputFlashcard(deckId, question, answer)
                onSave()
            },
            enabled = question.isNotBlank() && answer.isNotBlank(),
            modifier = Modifier
                .align(Alignment.End)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MagentaSecondary,
                disabledContainerColor = MagentaSecondary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Salvar")
        }
    }
}