package profile.model

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class ProfileImageResolver @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val contentResolver: ContentResolver = context.contentResolver

    suspend fun prepare(reference: profile.model.ProfileImageReference): PreparedProfileImage =
        withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(reference.contentUri)
                if (uri.scheme != ContentResolver.SCHEME_CONTENT) {
                    throw ProfileValidationException(FIELD_IMAGE)
                }

                val mimeType = contentResolver.getType(uri)?.lowercase()
                    ?: throw ProfileValidationException(FIELD_IMAGE_MIME_TYPE)
                if (mimeType !in ALLOWED_MIME_TYPES) {
                    throw ProfileValidationException(FIELD_IMAGE_MIME_TYPE)
                }

                val metadata = readMetadata(uri)
                val size = metadata.size?.takeIf { it >= 0L } ?: measureSize(uri)
                if (size !in 1..MAX_FILE_SIZE_BYTES) {
                    throw ProfileValidationException(FIELD_IMAGE_SIZE)
                }

                PreparedProfileImage(
                    uri = uri,
                    mimeType = mimeType,
                    size = size,
                    fileName = sanitizeFileName(metadata.displayName ?: defaultFileName(mimeType)),
                )
            } catch (error: ProfileValidationException) {
                throw error
            } catch (_: SecurityException) {
                throw ProfileValidationException(FIELD_IMAGE)
            } catch (_: IOException) {
                throw ProfileValidationException(FIELD_IMAGE)
            }
        }

    fun open(uri: Uri) = contentResolver.openInputStream(uri)
        ?: throw IOException("Unable to open the selected image")

    private fun readMetadata(uri: Uri): ImageMetadata {
        val cursor: Cursor = contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null,
        ) ?: return ImageMetadata()

        return cursor.use {
            if (!it.moveToFirst()) return@use ImageMetadata()
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            ImageMetadata(
                displayName = nameIndex.takeIf { index -> index >= 0 && !it.isNull(index) }
                    ?.let(it::getString),
                size = sizeIndex.takeIf { index -> index >= 0 && !it.isNull(index) }
                    ?.let(it::getLong),
            )
        }
    }

    private fun measureSize(uri: Uri): Long = open(uri).use { stream ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val count = stream.read(buffer)
            if (count < 0) break
            total += count
            if (total > MAX_FILE_SIZE_BYTES) break
        }
        total
    }

    private fun sanitizeFileName(value: String): String = value
        .replace("\r", "_")
        .replace("\n", "_")
        .replace("\"", "_")
        .take(MAX_FILE_NAME_LENGTH)
        .ifBlank { DEFAULT_FILE_NAME }

    private fun defaultFileName(mimeType: String): String = when (mimeType) {
        "image/jpeg" -> "profile-image.jpg"
        "image/png" -> "profile-image.png"
        "image/webp" -> "profile-image.webp"
        else -> DEFAULT_FILE_NAME
    }

    private data class ImageMetadata(
        val displayName: String? = null,
        val size: Long? = null,
    )

    companion object {
        const val MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L
        const val FIELD_IMAGE = "image"
        const val FIELD_IMAGE_MIME_TYPE = "image.mime_type"
        const val FIELD_IMAGE_SIZE = "image.size"
        private const val MAX_FILE_NAME_LENGTH = 255
        private const val DEFAULT_FILE_NAME = "profile-image"
        private val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp")
    }
}

data class PreparedProfileImage(
    val uri: Uri,
    val mimeType: String,
    val size: Long,
    val fileName: String,
)
