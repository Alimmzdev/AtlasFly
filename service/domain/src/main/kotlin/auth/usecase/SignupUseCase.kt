package auth.usecase

import auth.model.AuthProvider
import auth.model.AuthResult
import auth.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignupUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(provider: AuthProvider.EmailPassword): Flow<AuthResult> {
        return authRepository.signup(provider =provider)
    }
}