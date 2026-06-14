package com.example.a221003_ng_nelson_project2.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices

class LocationHelper(
    private val context: Context
) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onLocationReceived: (Double, Double) -> Unit
    ) {

        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            null
        )
            .addOnSuccessListener { location ->

                if (location != null) {

                    onLocationReceived(
                        location.latitude,
                        location.longitude
                    )
                }
            }
    }
}