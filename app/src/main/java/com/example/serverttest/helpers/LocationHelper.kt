package com.example.serverttest.helpers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.io.IOException
import java.util.Locale

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun getCurrentLocationString(callback: (String) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle the case where location permissions are not granted
            callback("Location permission not granted")
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 2000
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation: Location = locationResult.lastLocation
                lastLocation.let {
                    val latitude = lastLocation.latitude
                    val longitude = lastLocation.longitude

                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses: List<Address>?

                    try {
                        addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback("Error: ${e.message}")
                        fusedLocationClient.removeLocationUpdates(this)
                        return
                    }

                    if (!addresses.isNullOrEmpty()) {
                        val address: Address = addresses[0]
                        val addressString = address.getAddressLine(0) // Get the first address line
                        callback(addressString)
                    } else {
                        callback("Address not found")
                    }

                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
}

