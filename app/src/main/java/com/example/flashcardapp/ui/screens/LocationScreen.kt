package com.example.flashcardapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.flashcardapp.data.entities.StudyLocation
import com.example.flashcardapp.services.LocationService
import com.example.flashcardapp.ui.viewmodel.FlashcardAppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private val BluePrimary = Color(0xFF2962FF) // Azul vibrante primário
private val BlueIcons = Color(0xFF5481FF) // Azul mais claro para ícones
private val BlueDark = Color(0xFF0039CB) // Azul escuro para elementos de destaque
private val BlueLight = Color(0xFFE3F2FD) // Azul claro para fundos secundários
private val MagentaSecondary = Color(0xFFE91E63) // Rosa/magenta para elementos complementares
private val MagentaLight = Color(0xFFFCE4EC) // Rosa claro para fundos sutis
private val PurpleTransition = Color(0xFF9C27B0) // Roxo para acento
private val NeutralLight = Color(0xFFFAFAFA) // Neutro claro para fundos
private val NeutralDark = Color(0xFF333333) // Neutro escuro para textos principais
private val ErrorRed = Color(0xFFE53935) // Vermelho para erros
private val InfoBlue = Color(0xFF29B6F6) // Azul para informações
private val AmberAccent = Color(0xFFFFAB00) // Âmbar para elementos que precisam de destaque especial


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    navController: NavController,
    viewModel: FlashcardAppViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationService = remember { LocationService(context) }
    val locationsUiState by viewModel.locationsUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados para dialogos
    var showAddLocationDialog by remember { mutableStateOf(false) }
    var showEditLocationDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<StudyLocation?>(null) }

    // Campos de adicionar e editar
    var locationName by remember { mutableStateOf("") }
    var editingLocation by remember { mutableStateOf<StudyLocation?>(null) }

    // Coordenadas temporárias
    var tempLatitude by remember { mutableStateOf(0.0) }
    var tempLongitude by remember { mutableStateOf(0.0) }

    // Permissão
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Quando a permissão for concedida prossiga
            locationService.getCurrentLocation { location ->
                if (location != null) {
                    locationName = ""
                    tempLatitude = location.latitude
                    tempLongitude = location.longitude
                    showAddLocationDialog = true
                } else {
                    // Mensagem de erro
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Não foi possível obter a localização atual")
                    }
                }
            }
        } else {
            // Mensagem da necessidade de permissão
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Permissão de localização é necessária para adicionar a localização atual")
            }
        }
    }

    Scaffold(
        containerColor = NeutralLight,
        topBar = {
            TopAppBar(
                title = { Text("Localizações de Estudo", color = NeutralDark) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.White,
                    navigationIconContentColor = BlueIcons
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = BlueIcons)
                    }
                }
            )
        },
        floatingActionButton = {
            if (locationsUiState.canAddMore) {
                FloatingActionButton(
                    onClick = {
                        // Confirmar permissão antes
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) -> {
                                // Pegar a localização atual
                                locationService.getCurrentLocation { location ->
                                    if (location != null) {
                                        locationName = ""
                                        tempLatitude = location.latitude
                                        tempLongitude = location.longitude
                                        showAddLocationDialog = true
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Não foi possível obter a localização atual")
                                        }
                                    }
                                }
                            }
                            else -> {
                                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    },
                    containerColor = MagentaSecondary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Localização")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            if (locationsUiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = BluePrimary
                )
            } else if (locationsUiState.locations.isEmpty()) {
                EmptyLocationsView(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(locationsUiState.locations) { location ->
                        LocationItem(
                            location = location,
                            onEditClick = {
                                editingLocation = location
                                locationName = location.name
                                tempLatitude = location.latitude
                                tempLongitude = location.longitude
                                showEditLocationDialog = true
                            },
                            onDeleteClick = {
                                currentLocation = location
                                showDeleteConfirmDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Adicionar local
            if (showAddLocationDialog) {
                LocationDialog(
                    title = "Adicionar Localização",
                    name = locationName,
                    onNameChange = { locationName = it },
                    onConfirm = {
                        coroutineScope.launch {
                            viewModel.addStudyLocation(
                                name = locationName,
                                latitude = tempLatitude,
                                longitude = tempLongitude
                            )
                        }
                        showAddLocationDialog = false
                    },
                    onDismiss = { showAddLocationDialog = false }
                )
            }

            // Editar local
            if (showEditLocationDialog && editingLocation != null) {
                LocationDialog(
                    title = "Editar Localização",
                    name = locationName,
                    onNameChange = { locationName = it },
                    onConfirm = {
                        editingLocation?.let { location ->
                            coroutineScope.launch {
                                // Since there's no direct update method, we delete and recreate
                                viewModel.deleteLocation(location)
                                viewModel.addStudyLocation(
                                    name = locationName,
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                            }
                        }
                        showEditLocationDialog = false
                    },
                    onDismiss = { showEditLocationDialog = false }
                )
            }

            // Deletar
            if (showDeleteConfirmDialog && currentLocation != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    containerColor = Color.White,
                    titleContentColor = NeutralDark,
                    textContentColor = NeutralDark,
                    title = { Text("Excluir Localização") },
                    text = { Text("Tem certeza que deseja excluir '${currentLocation?.name}'? Esta ação não pode ser desfeita.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                currentLocation?.let { location ->
                                    coroutineScope.launch {
                                        viewModel.deleteLocation(location)
                                    }
                                }
                                showDeleteConfirmDialog = false
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
                            onClick = { showDeleteConfirmDialog = false },
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
    }
}

@Composable
fun EmptyLocationsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.LocationOff,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = BlueIcons
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhuma localização de estudo ainda",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = NeutralDark

        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Adicione localizações para estudar flashcards com base em onde você está",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = NeutralDark.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationItem(
    location: StudyLocation,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formattedDate = remember(location.creationDate) {
        dateFormat.format(Date(location.creationDate))
    }

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
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = NeutralDark,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = BlueIcons
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MagentaSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Criado em: $formattedDate",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralDark.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Coordenadas: ${String.format("%.6f, %.6f", location.latitude, location.longitude)}",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralDark.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun LocationDialog(
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = NeutralDark,
        textContentColor = NeutralDark,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nome da Localização", color = NeutralDark.copy(alpha = 0.8f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MagentaSecondary,
                    disabledContainerColor = MagentaSecondary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = BluePrimary
                )
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = MagentaSecondary) },
        text = { Text(message, color = NeutralDark) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirmar", color = MagentaSecondary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = NeutralDark)
            }
        },
        containerColor = Color.White
    )
}