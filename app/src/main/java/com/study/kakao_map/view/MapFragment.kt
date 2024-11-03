package com.study.kakao_map.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.MapViewInfo
import com.kakao.vectormap.animation.Interpolation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.TrackingManager
import com.kakao.vectormap.label.TransformMethod
import com.kakao.vectormap.shape.DotPoints
import com.kakao.vectormap.shape.Polygon
import com.kakao.vectormap.shape.PolygonOptions
import com.kakao.vectormap.shape.PolygonStyles
import com.kakao.vectormap.shape.PolygonStylesSet
import com.kakao.vectormap.shape.ShapeAnimator
import com.kakao.vectormap.shape.animation.CircleWave
import com.kakao.vectormap.shape.animation.CircleWaves
import com.study.kakao_map.R
import com.study.kakao_map.config.KAKAO_MAP_KEY
import com.study.kakao_map.config.LocationHelper
import com.study.kakao_map.databinding.FragmentMapBinding
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MapFragment : Fragment() {
    private lateinit var binding: FragmentMapBinding
    private var mapView: MapView? = null
    private var locationLabel: Label? =null
    private var circleWavePolygon: Polygon? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater)
        checkPermission()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapView
        LocationHelper.locationInit(requireContext())
        showMapView()
    }


    private fun showMapView() {
        var isFirstLocationUpdate = true // 처음 위치 업데이트 여부를 확인

        //Kakao Map SDK 초기화
        KakaoMapSdk.init(requireContext(), KAKAO_MAP_KEY)
        mapView?.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API가 정상적으로 종료될 때 호출
                Log.d("KakaoMap", "onMapDestroy")
            }

            override fun onMapError(p0: Exception?) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출
                Log.e("KakaoMap", "onMapError")
            }

        }, object : KakaoMapReadyCallback() {
            @SuppressLint("MissingPermission")
            override fun onMapReady(map: KakaoMap) {
                // 정상적으로 인증이 완료되었을 때 호출
                Log.d("KakaoMap", "onMapReady")
                //나침반으로서 지도가 회전 시 방향에 따라 움직이는 MapWidget
                map.compass?.show()

                //항상 지도의 중심에 보이도록 설정하기 위해서는 trackingManager 사용
//                val trackingManager = map.trackingManager
//
                //위치 업뎃
                LocationHelper.startLocationUpdate(requireContext()) { lat, long ->
                    val locationLat: Double? = lat
                    val locationLong: Double? = long
                    Log.d("KakaoMap", "onMapReady_LatLong: $locationLat | $locationLong")

                    if (locationLat != null && locationLong != null) {
                        val position = LatLng.from(locationLat, locationLong)
                        // 처음 위치 업데이트 시 카메라를 현재 위치로 이동
                        if (isFirstLocationUpdate) {
                            map.moveCamera(CameraUpdateFactory.newCenterPosition(position))
                            isFirstLocationUpdate = false // 첫 위치 업데이트 이후로는 카메라 이동하지 않음
                        }
//                        //현재 위치로 카메라 이동
//                        map.moveCamera(CameraUpdateFactory.newCenterPosition(position))

                        //위치가 업데이트 될 때마다 Label 위치 업데이트
                        if (locationLabel != null) {
                            // 기존 Label의 위치만 업데이트
                            locationLabel?.moveTo(position)
                        } else {
                            // 위치를 표시할 Label이 없을 때만 새로 생성
                            val labelLayer = map.labelManager?.layer
                            locationLabel = labelLayer?.addLabel(
                                LabelOptions.from(position)
                                    .setRank(10)
                                    .setStyles(
                                        LabelStyles.from(
                                            LabelStyle.from(R.drawable.current_location2)
                                                .setAnchorPoint(0.5f, 0.5f)
                                        )
                                    )
                                    .setTransform(TransformMethod.AbsoluteRotation_Decal)
                            )
                            // 방향을 표시 할 Label 을 생성
                            val headingLabel = map.labelManager?.layer?.addLabel(
                                LabelOptions.from(position)
                                    .setRank(9)
                                    .setStyles(
                                        LabelStyles.from(
                                            LabelStyle.from(R.drawable.red_direction_area)
                                                .setAnchorPoint(0.5f, 1.0f)
                                        )
                                    )
                                    .setTransform(TransformMethod.AbsoluteRotation_Decal)
                            )
                            // headingLabel이 locationLabel과 함께 움직이도록 설정한다.
                            locationLabel?.addSharePosition(headingLabel)
                            /**
                             * 반지름 1 짜리 Polygon
                             */
                            circleWavePolygon = map.shapeManager?.layer?.addPolygon(
                                PolygonOptions.from("circlePolygon")
                                    .setVisible(true)
                                    .setDotPoints(DotPoints.fromCircle(position, 1.0f))
                                    .setStylesSet(
                                        PolygonStylesSet.from(
                                            PolygonStyles.from(Color.parseColor("#f55d44"))
                                        )
                                    )
                            )
                            // circleWavePolygon 이 현재 위치 Label 의 transform 에 따라 움직이도록 설정한다.
                            locationLabel?.addShareTransform(circleWavePolygon)
                        }
                        binding.gpsBtn.setOnClickListener {
                            getCircleWaveAnimator(map)?.stop()

                            map.moveCamera(CameraUpdateFactory.newCenterPosition(position))

                            circleWavePolygon?.show()
                            circleWavePolygon?.setPosition(locationLabel?.position)
                            Log.d("KakaoMap", "circleWavePolygon: ${locationLabel?.position}")

                            getCircleWaveAnimator(map)?.addPolygons(circleWavePolygon)
                            getCircleWaveAnimator(map)?.start()
                        }
                    }
//                    binding.gpsBtn.setOnClickListener {
//                        getCircleWaveAnimator(map)?.stop()
//
//                        circleWavePolygon?.show()
//                        circleWavePolygon?.setPosition(locationLabel?.position)
//                        Log.d("KakaoMap", "circleWavePolygon: ${locationLabel?.position}")
//
//                        getCircleWaveAnimator(map)?.addPolygons(circleWavePolygon)
//                        getCircleWaveAnimator(map)?.start()
//                    }
                }
            }
        })
    }

    private fun getCircleWaveAnimator(map: KakaoMap): ShapeAnimator? {
        var shapeAnimator = map.shapeManager?.getAnimator("circle_wave_anim")
        if (shapeAnimator == null) {
            val circleWaves = CircleWaves.from("circle_wave_anim",
                CircleWave.from(1f, 0f, 0f, 120f))
                .setHideShapeAtStop(true)
                .setInterpolation(Interpolation.CubicInOut)
                .setDuration(1500)
                .setRepeatCount(3)
            shapeAnimator = map.shapeManager?.addAnimator(circleWaves)
            circleWavePolygon?.show()
        }
        return shapeAnimator
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {   //12 이상
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value } // 모든 권한이 승인되었는지 확인
        if (allGranted) {
            Log.d("KakaoMap", "모든 권한이 허용됨")
        } else {
            Log.d("KakaoMap", "일부 권한이 거부됨")
            // TODO: 권한 거부에 대한 시나리오는 추후 반영
            activity?.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.resume()    // MapView 의 resume 호출
        Log.d("KakaoMap", "onResume()")
    }
    override fun onPause() {
        super.onPause()
        mapView?.pause()  // MapView 의 pause 호출
        Log.d("KakaoMap", "onPause()")
    }

    override fun onStop() {
        super.onStop()
        LocationHelper.stopLocationUpdate()
        Log.d("KakaoMap", "onStop()================================")
    }
}