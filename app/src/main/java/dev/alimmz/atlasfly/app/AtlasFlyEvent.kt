package dev.alimmz.atlasfly.app

sealed interface AtlasFlyEvent {

    data object Refresh : AtlasFlyEvent

    data object Logout : AtlasFlyEvent

    data class HandleDeepLink(val uri: android.net.Uri) : AtlasFlyEvent

    data object DeepLinkHandled : AtlasFlyEvent
}