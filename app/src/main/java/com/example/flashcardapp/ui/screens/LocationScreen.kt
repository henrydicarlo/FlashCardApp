package com.example.flashcardapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Dialog states
    var showAddLocationDialog by remember { mutableStateOf(false) }
    var showEditLocationDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<StudyLocation?>(null) }

    // Input fields for add/edit dialog
    var locationName by remember { mutableStateOf("") }
    var editingLocation by remember { mutableStateOf<StudyLocation?>(null) }

    // Temporary location coordinates
    var tempLatitude by remember { mutableStateOf(0.0) }
    var tempLongitude by remember { mutableStateOf(0.0) }

    // Permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // When permission is granted, proceed
            locationService.getCurrentLocation { location ->
                if (location != null) {
                    locationName = ""
                    tempLatitude = location.latitude
                    tempLongitude = location.longitude
                    showAddLocationDialog = true
                } else {
                    // Show error message
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Não foi possível obter a localização atual")
                    }
                }
            }
        } else {
            // Show message about permission denial
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Permissão de localização é necessária para adicionar a localização atual")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Localizações de Estudo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (locationsUiState.canAddMore) {
                FloatingActionButton(
                    onClick = {
                        // Check location permission before proceeding
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) -> {
                                // Get current location and show dialog
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
                                // Request permission
                                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    }
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
                .padding(paddingValues)
        ) {
            if (locationsUiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (locationsUiState.locations.isEmpty()) {
                EmptyLocationsView(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
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

            // Add Location Dialog
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

            // Edit Location Dialog
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

            // Delete Confirmation Dialog
            if (showDeleteConfirmDialog && currentLocation != null) {
                ConfirmDialog(
                    title = "Excluir Localização",
                    message = "Tem certeza que deseja excluir '${currentLocation?.name}'? Esta ação não pode ser desfeita.",
                    onConfirm = {
                        currentLocation?.let { location ->
                            coroutineScope.launch {
                                viewModel.deleteLocation(location)
                            }
                        }
                        showDeleteConfirmDialog = false
                    },
                    onDismiss = { showDeleteConfirmDialog = false }
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
            imageVector = Icons.Default.ArrowDropDown, //LocationOff
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhuma localização de estudo ainda",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Adicione localizações para estudar flashcards com base em onde você está",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
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

    ElevatedCard(
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
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar"
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Criado em: $formattedDate",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Coordenadas: ${String.format("%.6f, %.6f", location.latitude, location.longitude)}",
                style = MaterialTheme.typography.bodySmall
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
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nome da Localização") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = name.isNotBlank()
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}