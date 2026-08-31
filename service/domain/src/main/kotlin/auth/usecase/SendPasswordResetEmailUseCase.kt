package auth.usecase

import auth.model.AuthResult
import auth.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendPasswordResetEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(email: String): Flow<AuthResult> {
        return authRepository.sendPasswordResetEmail(email)
    }
}
