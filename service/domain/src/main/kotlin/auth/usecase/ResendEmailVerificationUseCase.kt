package auth.usecase

import auth.repository.AuthRepository
import auth.model.AuthResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ResendEmailVerificationUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<AuthResult> {
        return authRepository.resendEmailVerification()
    }
}
