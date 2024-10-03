package com.study.kakao_map.view

import android.Manifest
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
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
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
import com.kakao.vectormap.shape.PolygonOptions
import com.kakao.vectormap.shape.PolygonStyles
import com.kakao.vectormap.shape.PolygonStylesSet
import com.kakao.vectormap.shape.animation.CircleWave
import com.kakao.vectormap.shape.animation.CircleWaves
import com.study.kakao_map.R
import com.study.kakao_map.config.KAKAO_MAP_KEY
import com.study.kakao_map.databinding.FragmentMapBinding
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MapFragment : Fragment() {
    private lateinit var binding: FragmentMapBinding

    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    private var lastLocation: Location? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationLabel: Label? =null


    private val locationListener = LocationListener { location ->
        lastLocation = location
        //위치가 업데이트 될 때마다 Label 위치 업데이트
        Log.d("KakaoMap", "lastLocation : ${lastLocation!!.longitude} | ${lastLocation!!.longitude}")
        locationLabel?.moveTo(LatLng.from(lastLocation!!.latitude, lastLocation!!.longitude))
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { entry ->
            val permissionName = entry.key
            val isGranted = entry.value
            if (isGranted){
                //권한 승인 시
                Log.d("KakaoMap", "Permission : $permissionName 권한 허용됨")
            } else {
                //권한을 허용 안할 경우 로직을 구현해야 무한 루프에 빠지지 않는다.
                Log.d("KakaoMap", "Permission : $permissionName 권한 거부됨")
                Toast.makeText(requireContext(), "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
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

        showMapView()
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

                //나침반으로서 지도가 회전 시 방향에 따라 움직이는 MapWidget
                map.compass?.show()

                val trackingManager = map.trackingManager

                //마지막 위치 가져오기
                val position = lastLocation?.let {
                    Log.d("KakaoMap", "LatLong: ${it.latitude} | ${it.longitude}")
                    LatLng.from(it.latitude, it.longitude)  //lastLocation 값이 null 아닐경우
                } ?: map.cameraPosition?.position   // null 일 경우
                //현재 위치로 카메라 이동
                map.moveCamera(CameraUpdateFactory.newCenterPosition(position))

                //현재 위치를 표시할 Label 생성
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
                //방향을 표시 할 Label
                val headingLabel = labelLayer?.addLabel(
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
                val circleWavePolygon = map.shapeManager?.layer?.addPolygon(
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
        })
    }

    override fun onResume() {
        super.onResume()

        checkPermission()
        startLocationUpdates()

        binding.mapView.resume()    // MapView 의 resume 호출
    }

    override fun onStop() {
        super.onStop()
        fusedLocationProviderClient?.removeLocationUpdates(locationListener)
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()  // MapView 의 pause 호출
    }

    private fun checkPermission() {

        //이미 퍼미션이 허용되어있는지 확인 후 요청
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //권한이 있을 경우
            startLocationUpdates()
        } else {
            //권한 요청이 안되어 있을 경우
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.Builder(1000).build()
            val executor: ExecutorService = Executors.newSingleThreadExecutor()
            fusedLocationProviderClient?.requestLocationUpdates(locationRequest, executor, locationListener)
        }
    }

}