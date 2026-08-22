package auth.usecase

import auth.repository.AuthRepository
import auth.model.AuthResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VerifyEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(oobCode: String): Flow<AuthResult> {
        return authRepository.verifyEmail(oobCode)
    }
}
