package auth.repository

import auth.datasource.remote.AuthRemoteDatasource
import auth.model.AuthError
import auth.model.AuthProvider
import auth.model.AuthResult
import auth.model.ResetCodeResult
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
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

    override fun signup(provider: AuthProvider.EmailPassword): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        authRemoteDatasource.signup(provider = provider)
        emit(AuthResult.Success)
    }
        .catch { e -> emit(e.toAuthResultFailure()) }
        .flowOn(Dispatchers.IO)

    override fun verifyEmail(oobCode: String): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        authRemoteDatasource.verifyEmail(oobCode)
        emit(AuthResult.Success)
    }
        .catch { e -> emit(e.toAuthResultFailure()) }
        .flowOn(Dispatchers.IO)

    override suspend fun isEmailVerified(): Boolean {
        return authRemoteDatasource.isEmailVerified()
    }

    override fun resendEmailVerification(): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        authRemoteDatasource.resendEmailVerification()
        emit(AuthResult.Success)
    }
        .catch { e -> emit(e.toAuthResultFailure()) }
        .flowOn(Dispatchers.IO)

    override fun sendPasswordResetEmail(email: String): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        authRemoteDatasource.sendPasswordResetEmail(email)
        emit(AuthResult.Success)
    }
        .catch { e -> emit(e.toAuthResultFailure()) }
        .flowOn(Dispatchers.IO)

    override suspend fun verifyPasswordResetCode(oobCode: String): ResetCodeResult {
        return withContext(Dispatchers.IO) {
            try {
                val email = authRemoteDatasource.verifyPasswordResetCode(oobCode)
                ResetCodeResult.Valid(email)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                ResetCodeResult.Invalid(e.toAuthError())
            }
        }
    }

    override fun confirmPasswordReset(
        oobCode: String,
        newPassword: String,
    ): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        authRemoteDatasource.confirmPasswordReset(oobCode, newPassword)
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

private fun Throwable.toAuthResultFailure(): AuthResult.Failure =
    AuthResult.Failure(toAuthError())

private fun Throwable.toAuthError(): AuthError = when (this) {
    is FirebaseAuthWeakPasswordException -> AuthError.WeakPassword
    is FirebaseAuthInvalidCredentialsException -> AuthError.InvalidCredentials
    is FirebaseAuthInvalidUserException -> AuthError.UserNotFound
    is FirebaseAuthUserCollisionException -> AuthError.AccountExistsDifferentProvider
    is FirebaseAuthActionCodeException -> AuthError.InvalidActionCode
    is FirebaseTooManyRequestsException -> AuthError.TooManyAttempts
    is CancellationException -> AuthError.Cancelled
    is IOException -> AuthError.NetworkError
    else -> AuthError.Unknown()
}