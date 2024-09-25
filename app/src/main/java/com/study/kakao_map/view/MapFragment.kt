package com.study.kakao_map.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapViewInfo
import com.kakao.vectormap.animation.Interpolation
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.TransformMethod
import com.kakao.vectormap.shape.DotPoints
import com.kakao.vectormap.shape.PolygonOptions
import com.kakao.vectormap.shape.PolygonStyles
import com.kakao.vectormap.shape.PolygonStylesSet
import com.kakao.vectormap.shape.animation.CircleWave
import com.kakao.vectormap.shape.animation.CircleWaves
import com.study.kakao_map.R
import com.study.kakao_map.config.KAKAO_MAP_KEY
import com.study.kakao_map.databinding.FragmentMapBinding
import java.lang.Exception

class MapFragment : Fragment() {
    private lateinit var binding: FragmentMapBinding

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var latLng: LatLng? = null

    private val permissionList = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )


    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions() // -> 여러 권한
    ) { permissions ->
        permissions.entries.forEach { entry ->  //entry 는 map<key, value> 형태
            val permissionName = entry.key
            val isGranted = entry.value
            if (isGranted) {
                Log.d("KakaoMap", "Permission : $permissionName 권한 허용됨")
                // 권한이 허용된 경우에 대한 처리
//                getCurrentLocation()

                //버전 10이상 백그라운드에서 위치 정보를 사용하는 경우 별도의 권한이 필요
                requestBackgroundLocationPermission()
            } else {
                Log.d("KakaoMap", "Permission : $permissionName 권한 거부됨")
                // 권한이 거부된 경우에 대한 처리
                //다이얼로그를 이용하여 권한 설정 진입 필요
            }
        }
    }

    private val backgroundPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission() // -> 단일 권한
    ) { isGranted ->
        if (isGranted) {
            Log.d("KakaoMap", "백그라운드 위치 권한 허용됨")
            // 권한이 허용된 경우에 대한 처리
//            getCurrentLocation()
        } else {
            Log.d("KakaoMap", "백그라운드 위치 권한 거부됨")
            //다이얼로그 처리
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //위치 기반 서비스 FusedLocationProviderClient 선언
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        checkLocationPermission()

        showMapView()
    }

    private fun checkLocationPermission() {
        /**
         * ContextCompat 과 ActivityCompat 차이
         * ContextCompat : 현재 권한이 있는지 확인 -> PERMISSION_GRANTED 또는 PERMISSION_DENIED 값으로 권한 여부를 리턴
         * ActivityCompat : 안드로이드에서 지원하는 권한요청 다이얼로그를 보여줄 수 있음(권한 요청) -> registerForActivityResult(권한 요청 콜백)
         */
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //권한 있을 경우 : PERMISSION_GRANTED, 권한 없을 경우 : PERMISSION_DENIED
            //권한 설정 되어있을 경우
//            getCurrentLocation()
            //백그라운드 권한 처리
            requestBackgroundLocationPermission()
        } else {
            permissionLauncher.launch(permissionList)
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient?.lastLocation?.addOnSuccessListener { location ->
                if (location == null) {
                    Log.e("KakaoMap", "location get fail")
                } else {
                    Log.d("KakaoMap", "${location.latitude} , ${location.longitude}")
                }
            }
        } else {
            permissionLauncher.launch(permissionList)
        }
    }

    private fun showMapView() {
        //Kakao Map SDK 초기화
        KakaoMapSdk.init(requireContext(), KAKAO_MAP_KEY)

        binding.mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API가 정상적으로 종료될 때 호출
                Log.d("KakaoMap", "onMapDestroy")
            }

            override fun onMapError(p0: Exception?) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출
                Log.e("KakaoMap", "onMapError")
            }

        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                // 정상적으로 인증이 완료되었을 때 호출
                Log.d("KakaoMap", "onMapReady")
                addMarker(map)
//                startCircleWaveAnimation(map)  // CircleWaves 애니메이션 시작
            }

            override fun getZoomLevel(): Int {
                // 지도 시작 시 확대/축소 줌 레벨 설정
                return 15
            }

            override fun getPosition(): LatLng {
                // 지도 시작 시 위치 좌표를 설정
                return LatLng.from(37.406960, 127.115587);
            }

            override fun getMapViewInfo(): MapViewInfo {
                // 지도 시작 시 App 및 MapType 설정
                return super.getMapViewInfo()
            }
        })
    }

    private fun startCircleWaveAnimation(map: KakaoMap) {

        val circle =
            PolygonOptions.from(DotPoints.fromCircle(LatLng.from(37.566535, 126.9779692), 10.0f))
                .setStylesSet(PolygonStylesSet.from(PolygonStyles.from(Color.parseColor("#078c03"))))

        val shapeLayer = map.shapeManager?.layer
//        shapeLayer?.addPolygon(circle)
        //애니메이션 적용
        val polygon = shapeLayer?.addPolygon(circle)

        // 1. AnimationOptions 설정
        val circleWaves = CircleWaves.from("CircleWaveAnimatorId")
            .setRepeatCount(10)
            .setDuration(3000)
            .setInterpolation(Interpolation.CubicIn)
            .addCircleWave(CircleWave.from(0.7f, 0.0f, 0.0f, 10.0f))

        // 2. ShapeAnimator 생성
        val shapeAnimator = map.shapeManager?.addAnimator(circleWaves)
        shapeAnimator?.addPolygons(polygon)
        shapeAnimator?.start()
    }

    private fun addMarker(map: KakaoMap) {
        //현재 위치를 표시 할 Label
        val labelLayer = map.labelManager?.layer
        val locationLabel = labelLayer?.addLabel(
            LabelOptions.from(LatLng.from(37.566535, 126.9779692))
                .setRank(10)
                .setStyles(
                    LabelStyles.from(
                        LabelStyle.from(R.drawable.current_location2)
                            .setAnchorPoint(0.5f, 0.5f)
                    )
                )
                .setTransform(TransformMethod.AbsoluteRotation_Decal)
        )
        //방향을 표시 할 Label
        val headingLabel = labelLayer?.addLabel(
            LabelOptions.from(LatLng.from(37.566535, 126.9779692))
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

        //AnimationOptions_circleWaves
        /**
         * 반지름 1 짜리 Polygon
         */
        val circleWavePolygon = map.shapeManager?.layer?.addPolygon(
            PolygonOptions.from("circlePolygon")
                .setVisible(true)
                .setDotPoints(DotPoints.fromCircle(LatLng.from(37.566535, 126.9779692), 1.0f))
                .setStylesSet(
                    PolygonStylesSet.from(
                        PolygonStyles.from(Color.parseColor("#f55d44"))
                    )
                )
        )

        // circleWavePolygon 이 현재 위치 Label 의 transform 에 따라 움직이도록 설정한다.
        locationLabel?.addShareTransform(circleWavePolygon)

        //버튼을 눌렀을시 맵 중앙에 라벨 위치 시킬수 있는 동작 구현 가능(필요시)
//        val centerLabel = layer?.addLabel(option)
//        //라벨의 위치가 변하더라도 항상 화면 중앙에 위치할 수 있도록 trackingManager를 통해 tracking을 시작
//        val trackingManager = map.trackingManager
//        trackingManager?.startTracking(centerLabel)

    }

    override fun onResume() {
        super.onResume()
        binding.mapView.resume()    // MapView 의 resume 호출
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()  // MapView 의 pause 호출
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {   //권한 요청 되있는 경우
                Log.d("KakaoMap", "백그라운드 위치 권한 이미 허용됨")
//                getCurrentLocation()
            } else {
                //권한 요청
                backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }
}