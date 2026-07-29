package tech.nullexdev.atlasfly.app

sealed interface AtlasFlyEvent {

    data object Refresh : AtlasFlyEvent

    data object Logout : AtlasFlyEvent
}