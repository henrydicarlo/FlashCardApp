package com.example.flashcardapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flashcardapp.data.entities.Flashcard
import com.example.flashcardapp.data.entities.FlashcardType
import com.example.flashcardapp.ui.viewmodel.FlashcardAppViewModel
import kotlinx.coroutines.launch


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
            flashcards = viewModel.getAllFlashcardsForDeck(deckId)
            isLoading = false
        }
    }

    Scaffold(
        containerColor = NeutralLight,
        topBar = {
            TopAppBar(
                title = { Text(deck?.name ?: "Detalhes do Baralho", color = NeutralDark) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = NeutralDark,
                    navigationIconContentColor =  BlueIcons,
                    actionIconContentColor = BlueIcons
                ),
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
            FloatingActionButton(
                onClick = { showAddFlashcardDialog = true },
                containerColor = MagentaSecondary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Flashcard")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = BluePrimary
                    )
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
                            style = MaterialTheme.typography.titleMedium,
                            color = NeutralDark
                        )

                        Text(
                            text = "Clique no botão + para adicionar flashcards a este baralho",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = NeutralDark.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (deck?.description?.isNotBlank() == true) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = BlueLight
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = deck.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = NeutralDark,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = NeutralDark.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        items(flashcards) { flashcard ->
                            FlashcardCard(
                                flashcard = flashcard,
                                onDelete = {
                                    scope.launch {
                                        viewModel.deleteFlashcard(flashcard)
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {

                    Spacer(modifier = Modifier.width(12.dp))

                    // Pergunta (resumida)
                    Text(
                        text = flashcard.question,
                        style = MaterialTheme.typography.bodyLarge,
                        color = NeutralDark,
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Botões de ação
                Row {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Recolher" else "Expandir",
                            tint = BluePrimary
                        )
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MagentaSecondary
                        )
                    }
                }
            }

            // Conteúdo expandido
            if (expanded) {
                Divider(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth(),
                    color = NeutralDark.copy(alpha = 0.1f)
                )

                when (flashcard.type) {
                    FlashcardType.CLOZE -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = BlueLight
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Texto completo:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = BlueDark
                                )

                                Text(
                                    text = flashcard.fullText ?: flashcard.question,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NeutralDark,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MagentaLight
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Parte oculta:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MagentaSecondary
                                )

                                Text(
                                    text = flashcard.answer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NeutralDark,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                    FlashcardType.QUIZ -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = BlueLight
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Resposta correta:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = BlueDark
                                )

                                Text(
                                    text = flashcard.answer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NeutralDark,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Opções disponíveis
                        val optionsList = parseOptions(flashcard.options)
                        if (optionsList.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MagentaLight
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Opções:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MagentaSecondary
                                    )

                                    Column(
                                        modifier = Modifier.padding(top = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        optionsList.forEachIndexed { index, option ->
                                            Text(
                                                text = "${index + 1}. $option",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = NeutralDark
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Sem opções disponíveis",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeutralDark.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    else -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = BlueLight
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Resposta:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = BlueDark
                                )

                                Text(
                                    text = flashcard.answer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NeutralDark,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            titleContentColor = NeutralDark,
            textContentColor = NeutralDark,
            title = { Text("Excluir Flashcard") },
            text = { Text("Tem certeza que deseja excluir este flashcard?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MagentaSecondary,
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = BluePrimary
                    )
                ) {
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
        containerColor = Color.White,
        titleContentColor = BluePrimary,
        textContentColor = NeutralDark,
        title = { Text("Selecione o tipo de flashcard") },
        text = {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlashcardTypeButton(
                    type = FlashcardType.BASIC,
                    icon = Icons.Default.PlayArrow,
                    color = BluePrimary,
                    description = "Pergunta e resposta simples",
                    onClick = { onTypeSelected(FlashcardType.BASIC) }
                )

                FlashcardTypeButton(
                    type = FlashcardType.QUIZ,
                    icon = Icons.Default.PlayArrow,
                    color = MagentaSecondary,
                    description = "Questão com múltipla escolha",
                    onClick = { onTypeSelected(FlashcardType.QUIZ) }
                )

                FlashcardTypeButton(
                    type = FlashcardType.CLOZE,
                    icon = Icons.Default.PlayArrow,
                    color = PurpleTransition,
                    description = "Texto com lacunas para preencher",
                    onClick = { onTypeSelected(FlashcardType.CLOZE) }
                )

                FlashcardTypeButton(
                    type = FlashcardType.INPUT,
                    icon = Icons.Default.Create,
                    color = InfoBlue,
                    description = "Digitação de resposta",
                    onClick = { onTypeSelected(FlashcardType.INPUT) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MagentaSecondary
                )
            ) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlashcardTypeButton(
    type: FlashcardType,
    icon: ImageVector,
    color: Color,
    description: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(color.copy(alpha = 0.3f))
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f))
                    .border(1.dp, color.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = type.name,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = type.name.capitalize(),
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralDark.copy(alpha = 0.8f)
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

