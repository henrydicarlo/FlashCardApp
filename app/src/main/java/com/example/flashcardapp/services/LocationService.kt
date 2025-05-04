package com.example.flashcardapp.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

/**
 * Serviço de localização para determinar a localização atual
 */
class LocationService(private val context: Context) {
    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest = LocationRequest.create().apply {
        interval = 10 * 60 * 1000 // Atualiza a cada 10 minutos
        fastestInterval = 5 * 60 * 1000 // Mais rápido a cada 5 minutos
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY // Precisão equilibrada
    }

    private var locationCallback: LocationCallback? = null
    private var lastKnownLocation: Location? = null

    /**
     * Obtém a localização atual
     * @param callback Callback com a localização ou null se não disponível
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(callback: (Location?) -> Unit) {
        if (hasLocationPermission()) {
            // Se já temos uma localização recente (menos de 5 minutos), a usamos
            if (lastKnownLocation != null) {
                val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
                if (lastKnownLocation!!.time > fiveMinutesAgo) {
                    callback(lastKnownLocation)
                    return
                }
            }

            // Caso contrário, obtemos uma nova localização
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        lastKnownLocation = location
                        callback(location)
                    } else {
                        // Se não temos localização ainda, pedimos uma atualização
                        requestLocationUpdates { newLocation ->
                            lastKnownLocation = newLocation
                            callback(newLocation)
                            // Após obter uma localização, paramos as atualizações
                            stopLocationUpdates()
                        }
                    }
                }
                .addOnFailureListener {
                    callback(null)
                }
        } else {
            callback(null)
        }
    }

    /**
     * Inicia atualizações periódicas de localização
     * @param onLocationUpdate Callback para cada nova localização
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(onLocationUpdate: (Location) -> Unit) {
        if (!hasLocationPermission()) return

        // Remove atualizações anteriores se existirem
        stopLocationUpdates()

        // Cria um novo callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                if (lastLocation != null) {
                    lastKnownLocation = lastLocation
                    onLocationUpdate(lastLocation)
                }
            }
        }

        // Inicia as atualizações
        locationCallback?.let {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                it,
                Looper.getMainLooper()
            )
        }
    }

    /**
     * Solicita uma única atualização de localização
     */
    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates(onLocationUpdate: (Location) -> Unit) {
        if (!hasLocationPermission()) return

        // Cria um callback temporário
        val tempCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                if (lastLocation != null) {
                    onLocationUpdate(lastLocation)
                }
                // Remove o callback após receber uma localização
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        // Solicita uma atualização
        fusedLocationClient.requestLocationUpdates(
            LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                numUpdates = 1
            },
            tempCallback,
            Looper.getMainLooper()
        )
    }

    /**
     * Para as atualizações de localização
     */
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    /**
     * Verifica se temos permissão de localização
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}