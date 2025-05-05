package com.example.flashcardapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import kotlinx.coroutines.launch
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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
private val BluePrimary = Color(0xFF2962FF) // Azul vibrante primário
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

// Gradiente principal para elementos destacados
private val GradientPrimary = Brush.horizontalGradient(
    colors = listOf(MagentaSecondary, PurpleTransition, BluePrimary)
)

// Gradiente suave para fundos e elementos decorativos
private val GradientBackground = Brush.verticalGradient(
    colors = listOf(BlueLight.copy(alpha = 0.8f), MagentaLight.copy(alpha = 0.3f))
)

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
        containerColor = NeutralLight,
        topBar = {
            TopAppBar(
                title = { Text("", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary,
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
                                        color = MagentaSecondary,
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
                        Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = Color.White)
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
                containerColor = MagentaSecondary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Deck")
            }
        }
    ) { paddingValues ->
        val isRefreshing = remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        SwipeRefresh(
            state = SwipeRefreshState(isRefreshing.value),
            onRefresh = {
                isRefreshing.value = true
                coroutineScope.launch {
                    viewModel.refreshDeckList()
                    isRefreshing.value = false
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NeutralLight)
                    .padding(paddingValues)
            ) {
                if (deckListState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = BluePrimary
                    )
                } else if (deckListState.decks.isEmpty()) {
                    Text(
                        text = "Nenhum baralho encontrado. Crie um novo!",
                        color = NeutralDark,
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
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Título com Destaque Colorido
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MagentaSecondary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = deckWithStats.deck.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = NeutralDark
                )
            }

            if (!deckWithStats.deck.description.isNullOrEmpty()) {
                Text(
                    text = deckWithStats.deck.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralDark.copy(alpha = 0.8f),
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
                    color = if (deckWithStats.dueCardCount > 0) BluePrimary else NeutralDark.copy(alpha = 0.7f)
                )

                if (deckWithStats.dueCardCount > 0) {
                    Button(
                        onClick = onStudyClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Estudar")
                    }
                } else {
                    OutlinedButton(
                        onClick = onStudyClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BluePrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
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
        titleContentColor = NeutralDark,
        textContentColor = NeutralDark,
        title = {
            Text(
                "Criar Novo Baralho",
                color = BluePrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    label = { Text("Nome do Baralho", color = NeutralDark.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = NeutralDark.copy(alpha = 0.3f),
                        focusedTextColor = NeutralDark,
                        unfocusedTextColor = NeutralDark
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deckDescription,
                    onValueChange = { deckDescription = it },
                    label = { Text("Descrição (Opcional)", color = NeutralDark.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = NeutralDark.copy(alpha = 0.3f),
                        focusedTextColor = NeutralDark,
                        unfocusedTextColor = NeutralDark
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(deckName, deckDescription.ifEmpty { null }) },
                enabled = deckName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = Color.White,
                    disabledContainerColor = BluePrimary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Criar")
            }
        },
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