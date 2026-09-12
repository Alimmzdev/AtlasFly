package dev.alimmz.atlasfly.core.local.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.crypto.tink.Aead
import dev.alimmz.atlasfly.core.local.model.AuthSessionMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class AuthSessionMetadataSerializer(
    private val aead: Aead,
) : Serializer<AuthSessionMetadata> {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override val defaultValue: AuthSessionMetadata = AuthSessionMetadata()

    override suspend fun readFrom(input: InputStream): AuthSessionMetadata {
        return try {
            val encryptedBytes = input.readBytes()
            if (encryptedBytes.isEmpty()) {
                return defaultValue
            }
            val decryptedBytes = aead.decrypt(encryptedBytes, null)
            json.decodeFromString<AuthSessionMetadata>(decryptedBytes.decodeToString())
        } catch (error: Exception) {
            throw CorruptionException("Cannot read auth session metadata.", error)
        }
    }

    override suspend fun writeTo(t: AuthSessionMetadata, output: OutputStream) {
        val jsonBytes = json.encodeToString(AuthSessionMetadata.serializer(), t).toByteArray()
        val encryptedBytes = aead.encrypt(jsonBytes, null)
        withContext(Dispatchers.IO) {
            output.write(encryptedBytes)
        }
    }
}
