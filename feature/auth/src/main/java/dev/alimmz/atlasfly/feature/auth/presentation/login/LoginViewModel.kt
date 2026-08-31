package dev.alimmz.atlasfly.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.model.AuthError
import auth.model.AuthProvider
import auth.model.AuthProvider.EmailPassword
import auth.model.AuthProvider.Github
import auth.model.AuthProvider.Google
import auth.model.AuthResult
import auth.usecase.IsAuthorizedUseCase
import auth.usecase.LoginUseCase
import auth.usecase.LogoutUseCase
import auth.usecase.RefreshTokensUseCase
import auth.usecase.SignupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signupUseCase: SignupUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val isAuthorizedUseCase: IsAuthorizedUseCase,
    private val refreshTokensUseCase: RefreshTokensUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    private var loginJob: Job? = null

    init {
        checkAuthorized()
    }

    fun onIntent(intent: LoginUiIntent) {
        when (intent) {
            is LoginUiIntent.EmailLogin ->
                login(provider = EmailPassword(intent.email, intent.password))

            is LoginUiIntent.EmailSignup -> signup(provider = EmailPassword(intent.email, intent.password))

            is LoginUiIntent.GoogleLogin ->
                login(provider = Google(intent.idToken))

            is LoginUiIntent.GithubLogin ->
                login(provider = Github(intent.accessToken))

            is LoginUiIntent.RefreshTokens ->
                refreshTokens()

            is LoginUiIntent.Logout ->
                logout()

            is LoginUiIntent.DismissError ->
                _uiState.update { it.copy(error = null) }

            is LoginUiIntent.EmailChanged -> _uiState.update { it.copy(email = intent.value) }
            is LoginUiIntent.PasswordChanged -> _uiState.update { it.copy(password = intent.value) }
            LoginUiIntent.ForgotPasswordClicked -> {
                viewModelScope.launch {
                    _events.send(LoginEvent.NavigateForgotPassword(_uiState.value.email))
                }
            }
            LoginUiIntent.NavigateToSignUp -> {}
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
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            loginUseCase(provider).collect { result ->
                when (result) {
                    is AuthResult.Loading -> _uiState.update {
                        it.copy(
                            isLoading = true,
                            loadingProvider = provider,
                            error = null,
                        )
                    }

                    is AuthResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loadingProvider = null,
                                isLoggedIn = true,
                            )
                        }
                        _events.send(LoginEvent.NavigateHome)
                    }

                    is AuthResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loadingProvider = null,
                                error = result.error.takeIf { e ->
                                    e !is AuthError.Cancelled && e !is AuthError.UserNotFound
                                },
                            )
                        }
                        if (result.error is AuthError.InvalidCredentials) {
                            _events.send(LoginEvent.ShowSignUpDialog)
                        }
                    }
                }
            }
        }
    }

    private fun signup(provider: EmailPassword) {
        viewModelScope.launch {
            signupUseCase.invoke(provider = provider)
                .collect { result ->
                    when (result) {
                        is AuthResult.Loading -> _uiState.update {
                            it.copy(
                                isLoading = true,
                                loadingProvider = provider,
                                error = null,
                            )
                        }

                        is AuthResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    loadingProvider = null,
                                    isLoggedIn = true,
                                )
                            }
                            _events.send(LoginEvent.NavigateSignupEmailVerification)
                        }

                        is AuthResult.Failure -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    loadingProvider = null,
                                    error = result.error.takeIf { e ->
                                        e !is AuthError.Cancelled && e !is AuthError.UserNotFound
                                    },
                                )
                            }
                            if (result.error is AuthError.InvalidCredentials) {
                                _events.send(LoginEvent.ShowSignUpDialog)
                            }
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
