package com.example.flashcardapp.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flashcardapp.ui.model.DeckWithStats
import com.example.flashcardapp.ui.viewmodel.FlashcardAppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    navController: NavController,
    viewModel: FlashcardAppViewModel
) {
    val deckListState by viewModel.deckListUiState.collectAsState()
    var showCreateDeckDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flashcards") },
                actions = {
                    IconButton(onClick = { navController.navigate("stats") }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Statistics")
                    }
                    IconButton(onClick = { navController.navigate("locations") }) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Locations")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDeckDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Deck")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (deckListState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (deckListState.decks.isEmpty()) {
                Text(
                    text = "Nenhum baralho encontrado. Crie um novo!",
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = deckWithStats.deck.name,
                style = MaterialTheme.typography.titleMedium
            )

            if (!deckWithStats.deck.description.isNullOrEmpty()) {
                Text(
                    text = deckWithStats.deck.description,
                    style = MaterialTheme.typography.bodyMedium,
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
                    style = MaterialTheme.typography.bodySmall
                )

                if (deckWithStats.dueCardCount > 0) {
                    Button(onClick = onStudyClick) {
                        Text("Estudar")
                    }
                } else {
                    OutlinedButton(onClick = onStudyClick) {
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
        title = { Text("Criar Novo Baralho") },
        text = {
            Column {
                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    label = { Text("Nome do Baralho") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deckDescription,
                    onValueChange = { deckDescription = it },
                    label = { Text("Descrição (Opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(deckName, deckDescription.ifEmpty { null }) },
                enabled = deckName.isNotBlank()
            ) {
                Text("Criar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}