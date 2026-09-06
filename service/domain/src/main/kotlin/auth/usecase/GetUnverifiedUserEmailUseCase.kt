package auth.usecase

import auth.repository.AuthRepository
import javax.inject.Inject

class GetUnverifiedUserEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): String? {
        return authRepository.getUnverifiedUserEmail()
    }
}
