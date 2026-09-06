package auth.usecase

import auth.model.ResetCodeResult
import auth.repository.AuthRepository
import javax.inject.Inject

class VerifyPasswordResetCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(oobCode: String): ResetCodeResult {
        return authRepository.verifyPasswordResetCode(oobCode)
    }
}
