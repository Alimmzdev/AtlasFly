package tech.nullexdev.atlasfly

sealed interface AtlasFlyEvent {

    data object Refresh : AtlasFlyEvent

    data object Logout : AtlasFlyEvent
}