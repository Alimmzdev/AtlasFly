package auth.repository

import auth.datasource.remote.AuthRemoteDatasource
import auth.model.AuthError
import auth.model.AuthProvider
import auth.model.AuthResult
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class AuthRepositoryImpl @Inject constructor(
    private val authRemoteDatasource: AuthRemoteDatasource,
) : AuthRepository {

    override suspend fun isAuthorized(): Boolean {
        return authRemoteDatasource.isAuthorized()
    }

    override fun login(provider: AuthProvider): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        authRemoteDatasource.login(provider)
        emit(AuthResult.Success)
    }
        .catch { e -> emit(e.toAuthResultFailure()) }
        .flowOn(Dispatchers.IO)

    override fun refreshTokens(): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        authRemoteDatasource.refreshTokens()
        emit(AuthResult.Success)
    }
        .catch { e -> emit(e.toAuthResultFailure()) }
        .flowOn(Dispatchers.IO)

    override suspend fun logout() {
        authRemoteDatasource.logout()
    }
}

private fun Throwable.toAuthResultFailure(): AuthResult.Failure = when (this) {
    is FirebaseAuthInvalidCredentialsException -> AuthResult.Failure(AuthError.InvalidCredentials)
    is FirebaseAuthInvalidUserException -> AuthResult.Failure(AuthError.UserNotFound)
    is FirebaseAuthUserCollisionException -> AuthResult.Failure(AuthError.AccountExistsDifferentProvider)
    is FirebaseTooManyRequestsException -> AuthResult.Failure(AuthError.TooManyAttempts)
    is CancellationException -> AuthResult.Failure(AuthError.Cancelled)
    is IOException -> AuthResult.Failure(AuthError.NetworkError)
    else -> AuthResult.Failure(AuthError.Unknown())
}