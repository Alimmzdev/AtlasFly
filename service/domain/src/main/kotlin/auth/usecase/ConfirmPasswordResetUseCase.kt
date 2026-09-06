package auth.usecase

import auth.model.AuthResult
import auth.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConfirmPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(oobCode: String, newPassword: String): Flow<AuthResult> {
        return authRepository.confirmPasswordReset(oobCode, newPassword)
    }
}
