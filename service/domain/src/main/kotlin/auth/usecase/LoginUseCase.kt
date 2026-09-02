package auth.usecase

import auth.model.AuthProvider
import auth.model.AuthResult
import auth.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(provider: AuthProvider): Flow<AuthResult> {
        return authRepository.login(provider)
    }
}