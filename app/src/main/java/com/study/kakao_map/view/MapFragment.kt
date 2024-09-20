package com.study.kakao_map.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapViewInfo
import com.study.kakao_map.R
import com.study.kakao_map.config.KAKAO_MAP_KEY
import com.study.kakao_map.databinding.FragmentMapBinding
import java.lang.Exception

class MapFragment : Fragment() {
    private lateinit var binding: FragmentMapBinding
    private var kakaoMap : KakaoMap? = null

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

        }, object : KakaoMapReadyCallback(){
            override fun onMapReady(map: KakaoMap) {
                // 정상적으로 인증이 완료되었을 때 호출
                Log.d("KakaoMap", "onMapReady")
                kakaoMap = map
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

    override fun onResume() {
        super.onResume()
        binding.mapView.resume()    // MapView 의 resume 호출
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()  // MapView 의 pause 호출
    }
}