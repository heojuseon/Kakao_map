package com.study.kakao_map.config

import android.Manifest
import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionCheck {
    private const val LOCATION_PERMISSION_REQUEST_CODE = 321
    private const val LOCATION_BACKGROUND_PERMISSION_REQUEST_CODE = 3211

//    fun checkLocationPermission(context: Context) {
//        ActivityCompat.requestPermissions(
//            context,
//            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
//            LOCATION_PERMISSION_REQUEST_CODE
//        )
//    }
}