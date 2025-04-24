package com.example.flashcardapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flashcardapp.data.entities.Flashcard
import com.example.flashcardapp.data.entities.FlashcardType
import com.example.flashcardapp.ui.viewmodel.FlashcardAppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    deckId: Long,
    navController: NavController,
    viewModel: FlashcardAppViewModel
) {
    val scope = rememberCoroutineScope()
    val deckListState by viewModel.deckListUiState.collectAsState()
    val deck = deckListState.decks.find { it.deck.deckId == deckId }?.deck
    var flashcards by remember { mutableStateOf<List<Flashcard>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddFlashcardDialog by remember { mutableStateOf(false) }

    // Carregar flashcards para este baralho
    LaunchedEffect(deckId) {
        scope.launch {
            isLoading = true
            // Em vez de repository.getFlashcardsForDeck, usamos uma função apropriada
            // do viewModel ou criamos uma
            flashcards = viewModel.getAllFlashcardsForDeck(deckId)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(deck?.name ?: "Detalhes do Baralho") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (!flashcards.isNullOrEmpty()) {
                        IconButton(onClick = { navController.navigate("study/$deckId") }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Estudar")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddFlashcardDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Flashcard")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                flashcards.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Nenhum flashcard encontrado",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Clique no botão + para adicionar flashcards a este baralho",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (deck?.description?.isNotBlank() == true) {
                            item {
                                Text(
                                    text = deck.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Divider()
                            }
                        }

                        items(flashcards) { flashcard ->
                            FlashcardCard(
                                flashcard = flashcard,
                                onDelete = {
                                    scope.launch {
                                        // Alterando para usar o viewModel para excluir
                                        viewModel.deleteFlashcard(flashcard)
                                        // Atualizar lista
                                        flashcards = viewModel.getAllFlashcardsForDeck(deckId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddFlashcardDialog) {
        AddFlashcardTypeDialog(
            onDismiss = { showAddFlashcardDialog = false },
            onTypeSelected = { type ->
                navController.navigate("create_flashcard/$deckId/$type")
                showAddFlashcardDialog = false
            }
        )
    }
}

@Composable
fun FlashcardCard(
    flashcard: Flashcard,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tipo de flashcard - Corrigindo ícones que não existem
                val typeIcon = when (flashcard.type) {
                    FlashcardType.BASIC -> Icons.Default.PlayArrow // Subject
                    FlashcardType.QUIZ -> Icons.Default.PlayArrow // QuestionAnswer
                    FlashcardType.CLOZE -> Icons.Default.PlayArrow // Substituindo TextFields // Notes
                    FlashcardType.INPUT -> Icons.Default.Create // Substituindo Edit
                }

                Icon(
                    typeIcon,
                    contentDescription = flashcard.type.name,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Pergunta (resumida)
                Text(
                    text = flashcard.question,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Botões de ação
                Row {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.PlayArrow else Icons.Default.PlayArrow, // ExpandLess e ExpandMore
                            contentDescription = if (expanded) "Recolher" else "Expandir"
                        )
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Conteúdo expandido
            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                when (flashcard.type) {
                    FlashcardType.CLOZE -> {
                        Text(
                            text = "Texto completo: ${flashcard.fullText ?: flashcard.question}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Text(
                            text = "Parte oculta: ${flashcard.answer}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    FlashcardType.QUIZ -> {
                        Text(
                            text = "Resposta correta: ${flashcard.answer}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        // Corrigindo o joinToString para o caso de options ser null
                        val optionsList = parseOptions(flashcard.options)
                        val optionsText = if (optionsList.isNotEmpty()) {
                            "Opções: ${optionsList.joinToString(", ")}"
                        } else {
                            "Sem opções disponíveis"
                        }


                        Text(
                            text = optionsText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = "Resposta: ${flashcard.answer}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Flashcard") },
            text = { Text("Tem certeza que deseja excluir este flashcard?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AddFlashcardTypeDialog(
    onDismiss: () -> Unit,
    onTypeSelected: (FlashcardType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecione o tipo de flashcard") },
        text = {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlashcardTypeButton(
                    type = FlashcardType.BASIC,
                    icon = Icons.Default.PlayArrow, // Subject
                    description = "Pergunta e resposta simples",
                    onClick = { onTypeSelected(FlashcardType.BASIC) }
                )

                FlashcardTypeButton(
                    type = FlashcardType.QUIZ,
                    icon = Icons.Default.PlayArrow, // QuestionAnswer
                    description = "Questão com múltipla escolha",
                    onClick = { onTypeSelected(FlashcardType.QUIZ) }
                )

                FlashcardTypeButton(
                    type = FlashcardType.CLOZE,
                    icon = Icons.Default.PlayArrow, // Notes
                    description = "Texto com lacunas para preencher",
                    onClick = { onTypeSelected(FlashcardType.CLOZE) }
                )

                FlashcardTypeButton(
                    type = FlashcardType.INPUT,
                    icon = Icons.Default.Create,
                    description = "Digitação de resposta",
                    onClick = { onTypeSelected(FlashcardType.INPUT) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun FlashcardTypeButton(
    type: FlashcardType,
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = type.name,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column {
                Text(
                    text = type.name.capitalize(),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Função de extensão para capitalizar strings
private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

fun parseOptions(options: String?): List<String> {
    return options?.split(";")?.map { it.trim() } ?: emptyList()
}
