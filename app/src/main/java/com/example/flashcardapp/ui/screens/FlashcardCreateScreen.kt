package com.example.flashcardapp.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flashcardapp.ui.viewmodel.FlashcardAppViewModel

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
        topBar = {
            TopAppBar(
                title = {
                    when (type) {
                        "BASIC" -> Text("Criar Flashcard Básico")
                        "QUIZ" -> Text("Criar Flashcard de Quiz")
                        "CLOZE" -> Text("Criar Flashcard Cloze")
                        "INPUT" -> Text("Criar Flashcard de Entrada")
                        else -> Text("Criar Flashcard")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Salvar via Handler específico */ },
                        enabled = formValid
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Salvar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
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
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            label = { Text("Pergunta") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = answer,
            onValueChange = { answer = it },
            label = { Text("Resposta") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
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
                .height(48.dp)
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
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            label = { Text("Pergunta") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Resposta correta:",
            style = MaterialTheme.typography.titleSmall
        )

        OutlinedTextField(
            value = correctAnswer,
            onValueChange = { correctAnswer = it },
            label = { Text("Opção correta") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Opções incorretas:",
            style = MaterialTheme.typography.titleSmall
        )

        OutlinedTextField(
            value = wrongOption1,
            onValueChange = { wrongOption1 = it },
            label = { Text("Opção incorreta 1") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = wrongOption2,
            onValueChange = { wrongOption2 = it },
            label = { Text("Opção incorreta 2") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = wrongOption3,
            onValueChange = { wrongOption3 = it },
            label = { Text("Opção incorreta 3 (opcional)") },
            modifier = Modifier.fillMaxWidth()
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
                .height(48.dp)
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
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = fullText,
            onValueChange = { fullText = it },
            label = { Text("Texto completo") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = hiddenText,
            onValueChange = { hiddenText = it },
            label = { Text("Texto a esconder") },
            modifier = Modifier.fillMaxWidth()
        )

        if (fullText.isNotBlank() && hiddenText.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pré-visualização:",
                style = MaterialTheme.typography.titleSmall
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (fullText.contains(hiddenText))
                            fullText.replace(hiddenText, "...")
                        else
                            "O texto a esconder deve estar contido no texto completo",
                        style = MaterialTheme.typography.bodyMedium
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
                .height(48.dp)
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
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            label = { Text("Pergunta") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = answer,
            onValueChange = { answer = it },
            label = { Text("Resposta esperada") },
            modifier = Modifier.fillMaxWidth()
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
                .height(48.dp)
        ) {
            Text("Salvar")
        }
    }
}