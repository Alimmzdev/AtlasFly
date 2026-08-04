package com.alimmzdev.atlasfly.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.model.AuthProvider
import auth.model.AuthResult
import auth.usecase.IsAuthorizedUseCase
import auth.usecase.LoginUseCase
import auth.usecase.LogoutUseCase
import auth.usecase.RefreshTokensUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val isAuthorizedUseCase: IsAuthorizedUseCase,
    private val refreshTokensUseCase: RefreshTokensUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<AuthUiState> = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthorized()
    }

    fun onIntent(intent: AuthUiIntent) {
        when (intent) {
            is AuthUiIntent.EmailLogin ->
                login(AuthProvider.EmailPassword(intent.email, intent.password))

            is AuthUiIntent.GoogleLogin ->
                login(AuthProvider.Google(intent.idToken))

            is AuthUiIntent.GithubLogin ->
                login(AuthProvider.Github(intent.accesToken))

            is AuthUiIntent.RefreshTokens ->
                refreshTokens()

            is AuthUiIntent.Logout ->
                logout()

            is AuthUiIntent.DismissError ->
                _uiState.update { it.copy(error = null) }
        }
    }

    private fun checkAuthorized() {
        viewModelScope.launch {
            val isAuthorized = isAuthorizedUseCase()
            _uiState.update { it.copy(isLoggedIn = isAuthorized) }
        }
    }

    private fun login(provider: AuthProvider) {
        viewModelScope.launch {
            loginUseCase(provider).collect { result ->
                _uiState.update {
                    when (result) {
                        is AuthResult.Loading -> it.copy(isLoading = true, error = null)
                        is AuthResult.Success -> it.copy(isLoading = false, isLoggedIn = true)
                        is AuthResult.Failure -> it.copy(isLoading = false, error = result.error)
                    }
                }
            }
        }
    }

    fun refreshTokens() {
        viewModelScope.launch {
            refreshTokensUseCase().collect { result ->
                _uiState.update {
                    when (result) {
                        is AuthResult.Loading -> it.copy(isLoading = true)
                        is AuthResult.Success -> it.copy(isLoading = false)
                        is AuthResult.Failure -> it.copy(isLoading = false, error = result.error)
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.update { it.copy(isLoggedIn = false) }
        }
    }
}