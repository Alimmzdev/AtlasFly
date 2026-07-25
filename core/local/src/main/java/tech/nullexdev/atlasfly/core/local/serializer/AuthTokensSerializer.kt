package tech.nullexdev.atlasfly.core.local.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.crypto.tink.Aead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import tech.nullexdev.atlasfly.core.local.model.AuthTokens
import java.io.InputStream
import java.io.OutputStream

class AuthTokensSerializer(
    private val aead: Aead
) : Serializer<AuthTokens> {

    override val defaultValue: AuthTokens = AuthTokens()

    override suspend fun readFrom(input: InputStream): AuthTokens {
        return try {
            val encryptedBytes = input.readBytes()
            if (encryptedBytes.isEmpty()) {
                return defaultValue
            }
            val decryptedBytes = aead.decrypt(encryptedBytes, null)
            Json.decodeFromString<AuthTokens>(decryptedBytes.decodeToString())
        } catch (e: Exception) {
            throw CorruptionException("Cannot read auth tokens.", e)
        }
    }

    override suspend fun writeTo(t: AuthTokens, output: OutputStream) {
        val jsonBytes = Json.encodeToString(AuthTokens.serializer(), t).toByteArray()
        val encryptedBytes = aead.encrypt(jsonBytes, null)
        withContext(Dispatchers.IO) {
            output.write(encryptedBytes)
        }
    }
}
