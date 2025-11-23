package jp.hotdrop.weeb.ui.navigation

sealed class AppDestination(val route: String) {
    data object BiometricAuth : AppDestination("biometric_auth")
    data object Main : AppDestination("main")
    data object Bookmark : AppDestination("bookmark")
}
