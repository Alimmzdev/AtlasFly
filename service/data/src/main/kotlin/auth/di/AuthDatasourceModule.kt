package auth.di

import auth.datasource.remote.AuthRemoteDatasource
import auth.datasource.remote.AuthRemoteDatasourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthDatasourceModule {

    @Binds
    abstract fun bindAuthRemoteDatasource(
        impl: AuthRemoteDatasourceImpl,
    ): AuthRemoteDatasource
}