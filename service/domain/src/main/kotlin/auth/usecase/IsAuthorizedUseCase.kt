package auth.usecase

import auth.repository.AuthRepository
import javax.inject.Inject

class IsAuthorizedUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Boolean {
        return authRepository.isAuthorized()
    }
}