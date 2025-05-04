package com.example.flashcardapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.flashcardapp.services.LocationService
import com.example.flashcardapp.ui.navigation.FlashcardAppNavHost
import com.example.flashcardapp.ui.theme.FlashCardAppTheme
import com.example.flashcardapp.ui.viewmodel.FlashcardAppViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: FlashcardAppViewModel by viewModels()
    private lateinit var locationService: LocationService

    // Solicitar permissão de localização
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (locationPermissionGranted) {
            // Inicia monitoramento de localização
            startLocationUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationService = LocationService(this)

        // Verifica permissão de localização
        checkAndRequestLocationPermission()

        setContent {
            FlashCardAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    FlashcardAppNavHost(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Atualiza a localização atual quando a app está em foco
        if (locationService.hasLocationPermission()) {
            viewModel.updateCurrentLocation()
        }
    }

    private fun checkAndRequestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permissão já concedida
                startLocationUpdates()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // O usuário já negou a permissão antes, mostrar explicação
                // Para simplificar, vamos apenas solicitar novamente
                requestLocationPermission()
            }
            else -> {
                // Solicitar permissão pela primeira vez
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun startLocationUpdates() {
        // Inicia monitoramento de localização em segundo plano
        locationService.startLocationUpdates { location ->
            // Atualiza a localização atual no ViewModel
            viewModel.updateCurrentLocation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Para o monitoramento de localização
        locationService.stopLocationUpdates()
    }
}