package auth.di

import auth.repository.AuthRepository
import auth.repository.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthRepositoryModule {

    @Binds
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl,
    ): AuthRepository
}