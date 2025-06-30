import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.study.kakao_map"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.study.kakao_map"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //추가
//        buildConfigField("String", "KAKAO_MAP_KEY", "\"${property("KAKAO_MAP_KEY")}\"")
        buildConfigField("String", "KAKAO_MAP_KEY", getApiKey("KAKAO_MAP_KEY"))
        //매니패스트에서 사용가능하게끔 설정
//        addManifestPlaceholders(mapOf("KAKAO_MAP_KEY" to getApiKey("KAKAO_MAP_KEY")))
        manifestPlaceholders["KAKAO_MAP_KEY"] = getApiKey("KAKAO_MAP_KEY")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        buildConfig = true
        viewBinding = true
    }
}

fun getApiKey(propertyKey: String): String {
    return gradleLocalProperties(rootDir).getProperty(propertyKey)
}

dependencies {
    //splash
    implementation("androidx.core:core-splashscreen:1.0.1")

    //모듈설정
    implementation ("com.kakao.sdk:v2-all:2.20.6") // 전체 모듈 설치, 2.11.0 버전부터 지원
//    implementation "com.kakao.sdk:v2-user:2.20.6" // 카카오 로그인 API 모듈
//    implementation "com.kakao.sdk:v2-share:2.20.6" // 카카오톡 공유 API 모듈
//    implementation "com.kakao.sdk:v2-talk:2.20.6" // 카카오톡 채널, 카카오톡 소셜, 카카오톡 메시지 API 모듈
//    implementation "com.kakao.sdk:v2-friend:2.20.6" // 피커 API 모듈
//    implementation "com.kakao.sdk:v2-navi:2.20.6" // 카카오내비 API 모듈
//    implementation "com.kakao.sdk:v2-cert:2.20.6" // 카카오톡 인증 서비스 API 모듈
    implementation ("com.kakao.maps.open:android:2.11.9")   //카카오지도 SDK 에 대한 모듈
    implementation ("com.google.android.gms:play-services-location:21.3.0") //V2 에서는 현위치 데이터를 제공하는 기능 없음 -> Google Play 서비스 Location API 를 이용

    //Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.1")


    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}