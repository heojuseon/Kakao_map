pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        //Kakao_Map
        maven(url = uri("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/"))
        //카카오V2 SDK를 사용하기 위한 일반적인 셋팅(로그인 api, 네비 api 등등..)
        maven { url = java.net.URI("https://devrepo.kakao.com/nexus/content/groups/public/") }
    }
}

rootProject.name = "Kakao_map"
include(":app")
