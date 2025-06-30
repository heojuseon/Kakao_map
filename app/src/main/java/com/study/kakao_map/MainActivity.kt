package com.study.kakao_map

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.study.kakao_map.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    //NavController 명시: NavHost 에 어떤 화면을 띄울 것인지 컨트롤 하는 역할을 수행
    private lateinit var navController: NavController

    private lateinit var splashScreen: SplashScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen = installSplashScreen().apply {
                // splashScreen 지속 시간 조절
                // ex ) ViewModel에서 데이터 로딩이 끝날 때까지 유지하고 싶은 경우에 활용
                setKeepOnScreenCondition {
                    true
                }
            }
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //navigation graph 가 작동할 위치에 set
        navController = binding.fragmentContainerView.getFragment<NavHostFragment>().navController
    }
}