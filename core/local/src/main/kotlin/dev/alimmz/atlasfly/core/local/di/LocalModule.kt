package dev.alimmz.atlasfly.core.local.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.alimmz.atlasfly.core.local.crypto.CryptoManager
import dev.alimmz.atlasfly.core.local.model.AuthSessionMetadata
import dev.alimmz.atlasfly.core.local.serializer.AuthSessionMetadataSerializer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Provides
    @Singleton
    fun provideAuthSessionMetadataDataStore(
        @ApplicationContext context: Context,
        cryptoManager: CryptoManager
    ): DataStore<AuthSessionMetadata> {
        return DataStoreFactory.create(
            serializer = AuthSessionMetadataSerializer(cryptoManager.aead),
            produceFile = { context.dataStoreFile("auth_tokens.pb") }
        )
    }
}
