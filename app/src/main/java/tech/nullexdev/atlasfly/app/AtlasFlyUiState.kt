package tech.nullexdev.atlasfly.app

data class AtlasFlyUiState(
    val isAuthorized: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)