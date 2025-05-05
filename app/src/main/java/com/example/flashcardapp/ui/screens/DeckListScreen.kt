package com.example.flashcardapp.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.room.Room
import com.example.flashcardapp.services.SyncService
import com.example.flashcardapp.ui.model.DeckWithStats
import com.example.flashcardapp.ui.viewmodel.FlashcardAppViewModel
import kotlin.math.roundToInt

// Definindo as cores da nova paleta
private val GreenPrimary = Color(0xFF4CAF50) // Verde principal
private val GreenDark = Color(0xFF388E3C) // Verde escuro para elementos de destaque
private val GreenLight = Color(0xFFC8E6C9) // Verde claro para fundos secundários
private val BlueDark = Color(0xFF1A237E) // Azul profundo para contraste
private val GreenPale = Color(0xFFE8F5E9) // Verde pálido para fundos
private val AmberAccent = Color(0xFFFFAB00) // Âmbar para elementos interativos
private val GrayLight = Color(0xFFF5F5F5) // Cinza claro para áreas de texto
private val GrayDark = Color(0xFF424242) // Cinza escuro para textos principais
private val RedError = Color(0xFFEF5350) // Vermelho para erros
private val BlueInfo = Color(0xFF42A5F5) // Azul para informações

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    navController: NavController,
    viewModel: FlashcardAppViewModel
) {
    val deckListState by viewModel.deckListUiState.collectAsState()
    val userStatsState by viewModel.userStatsUiState.collectAsState()
    var showCreateDeckDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = GreenPale,
        topBar = {
            TopAppBar(
                title = { Text("", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    Row(
                        modifier = Modifier.padding(start = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ícone de sequência de dias (chama)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Dias consecutivos",
                                tint = AmberAccent
                            )
                            Text(
                                text = "${userStatsState.streakDays}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Ícone de taxa de acerto
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = GreenDark,
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Taxa de acerto",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "${(userStatsState.correctAnswerRate * 100).roundToInt()}%",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sync() }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.downloadData() }) {
                        Icon(Icons.Default.Download, contentDescription = "Sync", tint = Color.White)
                    }
                    IconButton(onClick = { navController.navigate("stats") }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Statistics", tint = Color.White)
                    }
                    IconButton(onClick = { navController.navigate("locations") }) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Locations", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDeckDialog = true },
                containerColor = GreenDark,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Deck")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GreenPale)
                .padding(paddingValues)
        ) {
            if (deckListState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = GreenDark
                )
            } else if (deckListState.decks.isEmpty()) {
                Text(
                    text = "Nenhum baralho encontrado. Crie um novo!",
                    color = GrayDark,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(deckListState.decks) { deckWithStats ->
                        DeckCard(
                            deckWithStats = deckWithStats,
                            onClick = { navController.navigate("deck/${deckWithStats.deck.deckId}") },
                            onStudyClick = { navController.navigate("study/${deckWithStats.deck.deckId}") }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDeckDialog) {
        CreateDeckDialog(
            onDismiss = { showCreateDeckDialog = false },
            onCreate = { name, description ->
                viewModel.createDeck(name, description)
                showCreateDeckDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckCard(
    deckWithStats: DeckWithStats,
    onClick: () -> Unit,
    onStudyClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = deckWithStats.deck.name,
                style = MaterialTheme.typography.titleMedium,
                color = GrayDark
            )

            if (!deckWithStats.deck.description.isNullOrEmpty()) {
                Text(
                    text = deckWithStats.deck.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayDark.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cartões: ${deckWithStats.cardCount} (${deckWithStats.dueCardCount} para revisar)",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (deckWithStats.dueCardCount > 0) AmberAccent else BlueDark
                )

                if (deckWithStats.dueCardCount > 0) {
                    Button(
                        onClick = onStudyClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenDark,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Estudar")
                    }
                } else {
                    OutlinedButton(
                        onClick = onStudyClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = GreenDark
                        )
                    ) {
                        Text("Revisar")
                    }
                }
            }
        }
    }
}

@Composable
fun CreateDeckDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String?) -> Unit
) {
    var deckName by remember { mutableStateOf("") }
    var deckDescription by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = GrayDark,
        textContentColor = GrayDark,
        title = { Text("Criar Novo Baralho") },
        text = {
            Column {
                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    label = { Text("Nome do Baralho", color = GrayDark.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = GrayDark.copy(alpha = 0.5f),
                        focusedTextColor = GrayDark,
                        unfocusedTextColor = GrayDark
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deckDescription,
                    onValueChange = { deckDescription = it },
                    label = { Text("Descrição (Opcional)", color = GrayDark.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = GrayDark.copy(alpha = 0.5f),
                        focusedTextColor = GrayDark,
                        unfocusedTextColor = GrayDark
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(deckName, deckDescription.ifEmpty { null }) },
                enabled = deckName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenDark,
                    contentColor = Color.White,
                    disabledContainerColor = GreenDark.copy(alpha = 0.5f)
                )
            ) {
                Text("Criar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = BlueDark
                )
            ) {
                Text("Cancelar")
            }
        }
    )
}