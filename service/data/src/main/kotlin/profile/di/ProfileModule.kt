package profile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import profile.datasource.remote.ProfileRemoteDataSource
import profile.datasource.remote.ProfileRemoteDataSourceImpl
import profile.repository.ProfileRepository
import profile.repository.ProfileRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    abstract fun bindProfileRemoteDataSource(
        implementation: ProfileRemoteDataSourceImpl,
    ): ProfileRemoteDataSource

    @Binds
    abstract fun bindProfileRepository(
        implementation: ProfileRepositoryImpl,
    ): ProfileRepository
}
