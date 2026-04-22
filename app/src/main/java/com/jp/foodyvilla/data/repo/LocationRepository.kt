package com.jp.foodyvilla.data.repo

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

class LocationRepository(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Pair<Double, Double>? {

        return try {
            val location = fusedClient.lastLocation.await()

            location?.let {
                Pair(it.latitude, it.longitude)
            }

        } catch (e: Exception) {
            null
        }
    }
}