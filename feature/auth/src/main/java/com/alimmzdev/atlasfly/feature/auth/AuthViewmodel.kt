package com.alimmzdev.atlasfly.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.model.AuthError
import auth.model.AuthProvider
import auth.model.AuthProvider.*
import auth.model.AuthResult
import auth.usecase.IsAuthorizedUseCase
import auth.usecase.LoginUseCase
import auth.usecase.LogoutUseCase
import auth.usecase.RefreshTokensUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

    private var loginJob: Job? = null

    init {
        checkAuthorized()
    }

    fun onIntent(intent: AuthUiIntent) {
        when (intent) {
            is AuthUiIntent.EmailLogin ->
                login(EmailPassword(intent.email, intent.password))

            is AuthUiIntent.GoogleLogin ->
                login(Google(intent.idToken))

            is AuthUiIntent.GithubLogin ->
                login(Github(intent.accesToken))

            is AuthUiIntent.RefreshTokens ->
                refreshTokens()

            is AuthUiIntent.Logout ->
                logout()

            is AuthUiIntent.DismissError ->
                _uiState.update { it.copy(error = null) }

            is AuthUiIntent.EmailChanged -> _uiState.update { it.copy(email = intent.value) }
            is AuthUiIntent.PasswordChanged -> _uiState.update { it.copy(password = intent.value) }
            AuthUiIntent.ForgotPasswordClicked -> {}
            AuthUiIntent.NavigateToSignUp -> {}
        }
    }

    private fun checkAuthorized() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingAuth = true) }
            val isAuthorized = isAuthorizedUseCase()
            _uiState.update { it.copy(isLoggedIn = isAuthorized, isCheckingAuth = false) }
        }
    }

    private fun login(provider: AuthProvider) {
        loginJob?.cancel() // supersede any in-flight login attempt
        loginJob = viewModelScope.launch {
            loginUseCase(provider).collect { result ->
                _uiState.update {
                    when (result) {
                        is AuthResult.Loading -> it.copy(
                            isLoading = true,
                            loadingProvider = provider,
                            error = null,
                        )
                        is AuthResult.Success -> it.copy(
                            isLoading = false,
                            loadingProvider = null,
                            isLoggedIn = true,
                        )
                        is AuthResult.Failure -> it.copy(
                            isLoading = false,
                            loadingProvider = null,
                            error = result.error.takeIf { e -> e != AuthError.Cancelled },
                        )
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