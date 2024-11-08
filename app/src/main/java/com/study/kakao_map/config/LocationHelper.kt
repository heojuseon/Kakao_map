package com.study.kakao_map.config

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.DeviceOrientation
import com.google.android.gms.location.DeviceOrientationListener
import com.google.android.gms.location.DeviceOrientationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.FusedOrientationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object LocationHelper {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var fusedOrientationProviderClient: FusedOrientationProviderClient
    private var locationCallback: LocationCallback? = null

    fun locationInit(context: Context) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        fusedOrientationProviderClient = LocationServices.getFusedOrientationProviderClient(context)
    }

    fun startLocationUpdate(context: Context, locationResult: (Double?, Double?) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000
            ).build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    val location = locationResult.lastLocation
                    location?.let {
                        val lat = it.latitude
                        val long = it.longitude
                        Log.d("KakaoMap", "LocationHelper_Updated Location: $lat | $long")
                        locationResult(lat, long)
                    }
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } else {
            Log.e("KakaoMap", "Location_checkSelfPermission: DENIED")
        }
    }

    fun deviceOrientation(rotateResult: (Float) -> Unit) {
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val request = DeviceOrientationRequest.Builder(
            DeviceOrientationRequest.OUTPUT_PERIOD_DEFAULT
        ).build()
        val orientationListener = DeviceOrientationListener {
            val headingLabel = it.headingDegrees
            rotateResult(headingLabel)
        }
        fusedOrientationProviderClient.requestOrientationUpdates(
            request,
            executor,
            orientationListener
        )
    }

    fun stopLocationUpdate() {
        locationCallback?.let {
            fusedLocationProviderClient.removeLocationUpdates(it)
        }
    }
}