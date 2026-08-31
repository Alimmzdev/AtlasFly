package dev.alimmz.atlasfly.core.local.crypto

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import com.google.crypto.tink.RegistryConfiguration

@Singleton
class CryptoManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val KEYSET_NAME = "atlasfly_auth_keyset"
        private const val PREF_FILE_NAME = "atlasfly_auth_key_preference"
        private const val MASTER_KEY_URI = "android-keystore://_atlasfly_auth_master_key_"
    }

    val aead: Aead by lazy {
        AeadConfig.register()
        try {
            buildAead()
        } catch (_: Exception) {
            context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
                .edit {
                    clear()
                }
            buildAead()
    }
    }

    private fun buildAead(): Aead {
        return AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(
                RegistryConfiguration.get(),
                Aead::class.java
            )
    }

    fun encrypt(data: ByteArray, associatedData: ByteArray? = null): ByteArray {
        return aead.encrypt(data, associatedData)
    }

    fun decrypt(encryptedData: ByteArray, associatedData: ByteArray? = null): ByteArray {
        return aead.decrypt(encryptedData, associatedData)
    }
}