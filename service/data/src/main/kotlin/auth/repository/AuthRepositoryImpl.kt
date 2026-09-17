package auth.repository

import auth.datasource.local.AuthLocalDatasource
import auth.datasource.remote.AuthRemoteDatasource
import auth.model.AuthError
import auth.model.AuthProvider
import auth.model.AuthResult
import auth.model.ResetCodeResult
import dev.alimmz.atlasfly.core.local.model.AuthTokens
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
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
    private val authLocalDatasource: AuthLocalDatasource,
) : AuthRepository {

    override suspend fun isAuthorized(): Boolean {
        val remoteAuthorized = try {
            authRemoteDatasource.isAuthorized()
        } catch (error: AuthRestException) {
            if (error.errorCode != AuthErrorCode.UserNotFound) throw error

            clearInvalidSession()
            false
        }
        if (remoteAuthorized) {
            persistCurrentSession()
        } else if (authLocalDatasource.isAuthorized()) {
            authLocalDatasource.clearAuthTokens()
        }
        return remoteAuthorized
    }

    override fun login(provider: AuthProvider): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        authRemoteDatasource.login(provider)
        if (
            provider is AuthProvider.EmailPassword &&
            !authRemoteDatasource.isEmailVerified()
        ) {
            emit(AuthResult.Failure(AuthError.EmailNotVerified))
        } else {
            persistCurrentSession()
            emit(AuthResult.Success)
        }
    }
        .catch { e -> emit(e.toAuthResultFailure()) }
        .flowOn(Dispatchers.IO)

    override fun signup(provider: AuthProvider.EmailPassword): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        authRemoteDatasource.signup(provider = provider)
        authLocalDatasource.saveAuthTokens(
            AuthTokens(email = provider.email.trim(), emailVerified = false),
        )
        emit(AuthResult.Success)
    }
        .catch { e -> emit(e.toAuthResultFailure()) }
        .flowOn(Dispatchers.IO)

    override suspend fun isEmailVerified(): Boolean {
        val verified = authRemoteDatasource.isEmailVerified()
        if (verified) {
            persistCurrentSession()
        }
        return verified
    }

    override suspend fun getUnverifiedUserEmail(): String? {
        return authRemoteDatasource.getUnverifiedUserEmail()
            ?: authLocalDatasource.getAuthTokens().email.takeIf(String::isNotBlank)
    }

    override fun resendEmailVerification(email: String): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        authRemoteDatasource.resendEmailVerification(email)
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

    override suspend fun verifyPasswordRecoverySession(): ResetCodeResult {
        return withContext(Dispatchers.IO) {
            try {
                val email = authRemoteDatasource.verifyPasswordRecoverySession()
                ResetCodeResult.Valid(email)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                ResetCodeResult.Invalid(e.toAuthError())
            }
        }
    }

    override fun updatePassword(newPassword: String): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        authRemoteDatasource.updatePassword(newPassword)
        emit(AuthResult.Success)
    }
        .catch { e -> emit(e.toAuthResultFailure()) }
        .flowOn(Dispatchers.IO)

    override fun refreshTokens(): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        authRemoteDatasource.refreshTokens()
        persistCurrentSession()
        emit(AuthResult.Success)
    }
        .catch { e -> emit(e.toAuthResultFailure()) }
        .flowOn(Dispatchers.IO)

    override suspend fun logout() {
        authLocalDatasource.clearSessionMetadata()
        authRemoteDatasource.logout()
    }

    private suspend fun clearInvalidSession() {
        try {
            authRemoteDatasource.logout()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            // The server no longer knows this user, but the client session must
            // still be discarded so the next launch does not retry it.
        }
        authLocalDatasource.clearAuthTokens()
    }

    private suspend fun persistCurrentSession() {
        val session = authRemoteDatasource.getCurrentSession() ?: return
        if (session.hasVerifiedSession) {
            authLocalDatasource.saveSessionMetadata(session)
        }
    }
}

private fun Throwable.toAuthResultFailure(): AuthResult.Failure {
    if (this is CancellationException) throw this
    return AuthResult.Failure(toAuthError())
}

private fun Throwable.toAuthError(): AuthError = when (this) {
    is AuthRestException -> when (errorCode) {
        AuthErrorCode.WeakPassword -> AuthError.WeakPassword
        AuthErrorCode.InvalidCredentials -> AuthError.InvalidCredentials
        AuthErrorCode.UserNotFound -> AuthError.UserNotFound
        AuthErrorCode.UserAlreadyExists,
        AuthErrorCode.EmailExists,
        AuthErrorCode.IdentityAlreadyExists -> AuthError.AccountExistsDifferentProvider
        AuthErrorCode.EmailNotConfirmed -> AuthError.EmailNotVerified
        AuthErrorCode.OtpExpired,
        AuthErrorCode.BadCodeVerifier,
        AuthErrorCode.FlowStateExpired,
        AuthErrorCode.FlowStateNotFound -> AuthError.InvalidActionCode
        AuthErrorCode.OverRequestRateLimit,
        AuthErrorCode.OverEmailSendRateLimit -> AuthError.TooManyAttempts
        else -> AuthError.Unknown(cause = this)
    }
    is CancellationException -> AuthError.Cancelled
    is IOException -> AuthError.NetworkError
    else -> AuthError.Unknown(cause = this)
}
